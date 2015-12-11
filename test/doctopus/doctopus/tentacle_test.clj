(ns doctopus.doctopus.tentacle-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [doctopus.configuration :as cfg]
            [doctopus.db :as db]
            [doctopus.doctopus.head :as h]
            [doctopus.doctopus.tentacle :refer :all]
            [doctopus.test-database :refer [database-fixture]]
            [doctopus.test-utilities :as utils]
            [me.raynes.fs :as fs]))

(use-fixtures :once database-fixture)

(def test-map-props
  (h/parse-tentacle-config-map
   (edn/read-string (slurp (io/resource "test/heads/test/doctopus-test.edn")))))

(def one-tentacle (map->Tentacle test-map-props))

(deftest tentacle-test
  (db/save-tentacle! one-tentacle)
  (testing "Generating HTML"
    (is (not= nil (generate-html one-tentacle)) "This should return a truthy value on success")
    (is (< 0 (count-records one-tentacle))
        "There should be some HTML in the DB"))
  (testing "Can we correctly load the HTML entrypoint of this tentacle?"
    (let [loaded-entrypoint (get-html-entrypoint one-tentacle)]
      (is (= loaded-entrypoint "/docs/doctopus-test/index.html")))))
