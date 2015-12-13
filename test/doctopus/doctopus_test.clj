(ns doctopus.doctopus-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [doctopus.db :as db]
            [doctopus.db.schema :as schema]
            [doctopus.doctopus.head :as h]
            [doctopus.doctopus :refer :all]
            [doctopus.doctopus.tentacle :as t]
            [doctopus.test-database :refer [schema-and-content-fixture]]
            [doctopus.test-utilities :as utils]
            [taoensso.timbre :as log])
  (:import [doctopus.doctopus Doctopus]
           [doctopus.doctopus.head Head]))

(use-fixtures :once schema-and-content-fixture)

(def stunt-doctopus (Doctopus. {} {}))

;; ### A Note About this Stack of Tests
;; One might notice that, while we test route matching in tentacle_tests.clj, we
;; do _not_ do any route matching here, or in the tests for Head. This is
;; because those routes effectively dispacth down to the tentacle -- which has
;; to be able to match an arbirarily segmented URI, and thus has to do a GET "*"
;; match on the entire URI. _However_, that match _also_ must include
;; the "/docs" context, which is added to routes in web.clj. Therefore, routes
;; are tested two places: in tentacle_tests; in web_tests.

(deftest doctopus-test
  (testing "Can we list heads?"
    (let [heads (list-heads stunt-doctopus)]
      (is (not (empty? heads)))
      (is (= "main" (:name (first heads))))))
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
        "There should never, ever be a tentacle with this name. Ever.")))
