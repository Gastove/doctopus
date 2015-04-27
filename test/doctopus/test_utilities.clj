(ns doctopus.test-utilities
  (:require [clojure.test :refer :all]
            [doctopus.storage :refer [remove-from-storage backend]]
            [doctopus.test-utilities :refer :all])
  (:import [org.joda.time DateTime]
           [org.joda.time.format DateTimeFormat]))

(def iso-formatter (DateTimeFormat/forPattern "yyyy-MM-dd"))

(defn make-today
  []
  (let [today (DateTime.)]
    (.print iso-formatter today)))

(defn clean-up-test-html
  [k]
  (remove-from-storage backend k))
