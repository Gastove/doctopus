(ns doctopus.doctopus.tentacle-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [doctopus.configuration :as cfg]
            [doctopus.doctopus.tentacle :refer :all]
            [doctopus.storage :as storage]
            [doctopus.storage.impls.temp-fs :as temp-fs]
            [me.raynes.fs :as fs]))

(def test-map-props
  (let [base-map (edn/read-string (slurp (io/resource "self/heads/main/doctopus.edn")))]
    (assoc base-map :name "doctopus-test")))

(def one-tentacle (map->Tentacle test-map-props))

(storage/set-backend! :temp-fs)

(deftest tentacle-test
  (testing "Generating HTML"
    (is (not= nil (generate-html one-tentacle)) "This should return a truthy value on success")
    (binding [fs/*cwd* @temp-fs/temp-dir]
      (let [html-location (fs/file "doctopus-test/index.html")]
        (is (fs/exists? html-location)
            "There should be some HTML in a known location"))))
  (testing "Can we correctly load the HTML entrypoint of this tentacle?"
    (let [loaded-entrypoint (get-html-entrypoint one-tentacle)]
      (is (= loaded-entrypoint "/docs/doctopus-test/index.html")))))
