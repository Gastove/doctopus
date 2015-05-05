(ns doctopus.doctopus-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [doctopus.db :as db]
            [doctopus.doctopus.head :as h]
            [doctopus.doctopus :refer :all]
            [doctopus.doctopus.tentacle :as t]
            [doctopus.storage :as storage]
            [doctopus.test-database :refer [database-fixture]]
            [doctopus.test-utilities :as utils])
  (:import [doctopus.doctopus Doctopus]
           [doctopus.doctopus.head Head]))

(use-fixtures :once database-fixture)

(storage/set-backend! :temp-fs)

(def stunt-doctopus (Doctopus. {} {}))

(deftest doctopus-test
  (testing "Can we list heads?"
    (is (not (empty? (list-heads stunt-doctopus))))
    (is (= "main" (:name (first (list-heads stunt-doctopus))))))
  (testing "Can we list tentacles?"
    (let [tent-list (list-tentacles stunt-doctopus)]
      (is (= 1 (count tent-list))
          "We should only have one of these")
      (is (= "doctopus" (:name (first tent-list)))
          "Would sure be weird if we got a different tentacle here")))
  (testing "Can we get a tentacle by its head?"
    (is (= 1 (count (list-tentacles-by-head stunt-doctopus "main")))
        "This should get one tentacle")
    (is (empty? (list-tentacles-by-head stunt-doctopus "HOOBASTANK"))
        "There should never, ever be a tentacle with this name. Ever."))
  (utils/clean-up-test-html "doctopus"))
