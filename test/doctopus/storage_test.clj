(ns doctopus.storage-test
  (:require [doctopus.storage :refer :all]
            [clojure.test :refer :all]))


(deftest storage
  (testing "Can we swap backends?"
    (is (set-backend! :temp-fs)
        "Can we set the backend to a differen't implementation?")
    (is (set-backend! default-backend)
        "And can we set it back to the default")
    (is (set-backend! default-backend)
        "Setting should be idempotent -- setting the same backend twice shouldn't matter")
    (is (thrown? java.lang.RuntimeException (set-backend! backend :HOODYBOO))
        "Explode on an unsupported backend.")
    ;; Make sure we exit on the default
    (set-backend! default-backend)))
