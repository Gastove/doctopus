(ns doctopus.validation
  (:require [clojure.string :as s]
            [doctopus.util :refer [in?]]))

(enable-console-print!)

(defn field-empty?
  [contents]
  (empty? (s/trim contents)))

(defn field-duplicate-value?
  [field values value]
  (in? value (map #(get % field) values)))

(defn field-invalid-characters?
  ([contents] (field-invalid-characters? contents #"[\w-_]+"))
  ([contents rex]
   (and (not= contents "") (->> contents
                                 (re-matches rex)
                                 nil?))))
