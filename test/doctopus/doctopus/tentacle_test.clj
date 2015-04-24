(ns doctopus.doctopus.tentacle-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [doctopus.doctopus.tentacle :refer :all]
            [me.raynes.fs :as fs]))

(def test-map-props
  (let [base-map (edn/read-string (io/resource "resources/self/heads/main/doctopus"))]
    (assoc base-map :name "doctopus-test")))

(def one-tentacle (map->Tentacle ))
