(ns doctopus.doctopus-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [doctopus.doctopus.head :as h]
            [doctopus.doctopus :refer :all]
            [doctopus.storage :as storage]
            [doctopus.test-utilities :as utils])
  (:import [doctopus.doctopus Doctopus]
           [doctopus.doctopus.head Head]))

(def doctopus-test-configs
  {:heads-dir (io/resource "test/heads")})

;; Note: we *should* probably test bootstrapping heads here. HOWEVER, that's a
;; mechanism we're about to change in a major (and good) way, so I'm not super
;; inclined to mess with it until we get a Database and we can stop returning
;; new Doctopi/Heads every time there's a state change :/ - RMD
(def stunt-doctopus (bootstrap-heads (Doctopus. doctopus-test-configs)))

(storage/set-backend! :temp-fs)

(deftest doctopus-test
  (testing "Can we list heads?"
    (let [test-head (h/bootstrap-tentacles (Head. "test") (io/resource "test/heads"))]
      (is (= #{test-head} (set (list-heads stunt-doctopus))))
      (is (= "test" (:name (first (list-heads stunt-doctopus)))))))
  (testing "Can we list tentacles?"
    (let [tent-list (list-tentacles stunt-doctopus)]
      (is (= 1 (count tent-list))
          "We should only have one of these")
      (is (= "doctopus-test" (:name (first tent-list)))
          "Would sure be weird if we got a different tentacle here")))
  (testing "Can we get a tentacle by its head?"
    (is (= 1 (count (list-tentacles-by-head stunt-doctopus "test")))
        "This should get one tentacle")
    (is (empty? (list-tentacles-by-head stunt-doctopus "HOOBASTANK"))
        "There should never, ever be a tentacle with this name. Ever."))
  (utils/clean-up-test-html "doctopus-test"))
