(ns doctopus.validation
  (:require [clojure.string :as s]
            [doctopus.util :refer [in?]]))

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
  "Creates a validation function, suitable for an (add-watch) call.

  validation-map takes the following form:
    {:field-name [[validation-function :error-constant]]}, e.g.
    {:name [[empty? :name-empty]
            [nil? :name-nil]]}

  After performing validation, calls (cb error-list) where error-list is a
  vector of :error-constant where validation-function returned true"
  [validation-map cb]
  (fn [key atom old new]
    (cb (filter #(not (nil? %))
                (flatten (for [[field-name validators] validation-map]
                           (for [[f k] validators]
                             (if-let [value (get new field-name)]
                               (if (f value) k)
                               k))))))))
