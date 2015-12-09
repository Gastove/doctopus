(ns doctopus.validation
  (:require [clojure.string :as s]
            [cljs.core.async :refer [>! <! chan]]
            [doctopus.util :refer [in?]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defn field-empty?
  "Returns true if contents is empty, whitespace trimmed."
  [contents]
  (empty? (s/trim contents)))

(defn field-duplicate-value?
  "Returns true if value exists under field in each values."
  [field values value]
  (in? value (map #(get % field) values)))

(defn field-invalid-characters?
  ([contents] (field-invalid-characters? contents #"[\w-_]+"))
  ([contents rex]
   (and (not= contents "") (->> contents
                                 (re-matches rex)
                                 nil?))))

(defn create-form-validator
  "Creates a validator channel, given a validation map. Put values on the
  channel, and a validation result will be put on its output.

  validation-map takes the following form:
    {:field-name [[validation-function :error-constant]]}, e.g.
    {:name [[empty? :name-empty]
            [nil? :name-nil]]}

  After performing validation, puts error-list on the channel, which is a vector
  of :error-constant where validation-function returned true"
  [validation-map]
  (let [c (chan)]
    (go-loop []
      (let [new (<! c)]
        (>! c (filter #(not (nil? %))
                      (flatten (for [[field-name validators] validation-map]
                                 (for [[f k] validators]
                                   (if-let [value (get new field-name)]
                                     (if (f value) k)
                                     k)))))))
      (recur))
    c))
