(ns doctopus.test-utilities
  (:require [clojure.string :as str]
            [clojure.test :refer :all]
            [doctopus.doctopus.head :refer [->Head]]
            [doctopus.doctopus.tentacle :refer [map->Tentacle]])
  (:import [org.joda.time DateTime]
           [org.joda.time.format DateTimeFormat]))

(defn truthy? [v]
  (or (true? v)
      (and (not (nil? v)) (not (false? v)))))

(def iso-formatter (DateTimeFormat/forPattern "yyyy-MM-dd"))

(defn make-today
  []
  (let [today (DateTime.)]
    (.print iso-formatter today)))

;; Make mock requests
(defn fake-request
  [routes method uri & params]
  (routes {:request-method method :uri uri :params (first params)}))

;; Data-Mocking functions

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
