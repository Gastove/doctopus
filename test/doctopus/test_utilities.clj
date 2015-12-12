(ns doctopus.test-utilities
  (:require [clojure.string :as str]
            [clojure.test :refer :all]
            [doctopus.doctopus.head :refer [->Head]]
            [doctopus.doctopus.tentacle :refer [map->Tentacle]]
            [doctopus.storage :refer [remove-from-storage backend]])
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

(defmethod mock-data :tentacle
  [_ _]
  (map->Tentacle {:name (mock-data :string 10)
                  :html-commands [(mock-data :string 10)]
                  :output-root (mock-data :string 15)
                  :source-control "git"
                  :source-location (mock-data :string 10)
                  :entry-point (mock-data :string 10)}))

(defmethod mock-data :head
  [_ _]
  (->Head (mock-data :string 18)))
