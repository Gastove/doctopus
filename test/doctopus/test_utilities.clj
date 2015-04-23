(ns doctopus.test-utilities
  (:require [clojure.test :refer :all]
            [doctopus.test-utilities :refer :all])
  (:import [org.joda.time DateTime]
           [org.joda.time.format DateTimeFormat]))

(def iso-formatter (DateTimeFormat/forPattern "yyyy-MM-dd"))

(defn make-today
  []
  (let [today (DateTime.)]
    (.print iso-formatter today)))
