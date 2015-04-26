(ns doctopus.doctopus.head-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [doctopus.doctopus.head :refer :all]
            [doctopus.doctopus.tentacle :as t]
            [doctopus.storage :as storage]
            [doctopus.test-utilities :as utils])
  (:import [doctopus.doctopus.head Head]))

(def test-head (Head. "test"))

(def test-tentacle-props
  (edn/read-string (slurp (io/resource "test/heads/test/doctopus-test.edn"))))

(def one-tentacle (t/map->Tentacle test-tentacle-props))

(storage/set-backend! :temp-fs)

(defn get-tentacle-name-from-test-head
  [test-head]
  (let [tents (:tentacles test-head)
        tent (first tents)]
    (:name tent)))

(deftest head-test
  (testing "Can we bootstrap a Head's tentacles?"
    (let [new-head (bootstrap-tentacles test-head (io/resource "test/heads"))]
      (is (not (nil? (:tentacles new-head))) "Should have tentacles now")
      (is (= "test" (:name new-head)) "Name should be the same")
      (is (= "doctopus-test" (get-tentacle-name-from-test-head new-head)))
      (is (= one-tentacle (first (:tentacles new-head)))))
    (utils/clean-up-test-html "doctopus-test")))
