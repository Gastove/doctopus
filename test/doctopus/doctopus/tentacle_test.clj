(ns doctopus.doctopus.tentacle-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [doctopus.configuration :as cfg]
            [doctopus.doctopus.tentacle :refer :all]
            [doctopus.storage :as storage]
            [doctopus.storage.impls.temp-fs :as temp-fs]
            [doctopus.test-utilities :as utils]
            [me.raynes.fs :as fs]))

(def test-map-props
  (edn/read-string (slurp (io/resource "test/heads/test/doctopus-test.edn"))))

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
      (is (= loaded-entrypoint "/docs/doctopus-test/index.html"))))
  (utils/clean-up-test-html "doctopus-test"))
