(ns doctopus.test-utilities
  (:require [clojure.string :as str]
            [clojure.test :refer :all]
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

(defmulti mock-data (fn [kind length] kind))

(defmethod mock-data :int
  [_ length]
  (rand-int length))

(defmethod mock-data :string
  [_ length]
  (let [upper-alphas "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        lower-alphas (str/lower-case upper-alphas)
        nums "0123456789"
        punct-and-spaces " -!,?:~_ \"'$%&"
        candidate-chars (apply str
                               upper-alphas
                               lower-alphas
                               nums
                               punct-and-spaces)]
    (loop [acc []]
      (if (= (count acc) length)
        (apply str acc)
        (recur (conj acc (rand-nth candidate-chars)))))))
