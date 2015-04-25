(ns doctopus.storage-test
  (:require [doctopus.storage :refer :all]
            [clojure.test :refer :all]))


(deftest storage
  (testing "Can we swap backends?"
    (is (= default-backend (get-key-from-backend backend :name))
        "Are we using the default backend?")
    (is (set-backend! backend :temp-fs)
        "Can we set the backend to a differen't implementation?")
    (is (set-backend! backend default-backend)
        "And can we set it back to the default"))
  )
