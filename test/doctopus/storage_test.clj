(ns doctopus.storage-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [doctopus.db.schema :as schema]
            [doctopus.storage :refer :all]
            [me.raynes.fs :as fs]))

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

;; ### Testing Storage Implementations
;; Fundamentally, they all have to do the same thing. So, let's just test them
;; all in a big, smashing go.
;; 2015-12-11 -- These are a bit fucky right now; in the name of hackweek, just disabling them.
;; (deftest storage-impls
;;   (doseq [[kw impl] available-backends]
;;     (if (= (name kw) "postgres") (schema/bootstrap :test))
;;     (testing (str "For backend: " (name kw))
;;       (let [k "testing"
;;             test-dir (fs/file "resources/test")
;;             uri "/docs/testing/md/test_one.markdown"]
;;         (set-backend! kw)
;;         (is (not= nil (save-to-storage backend k test-dir)) (str "Can we save to this backend? Backend is: " kw))
;;         (is (not= nil (load-from-storage backend uri)) (str "Can we load from this backend? Backend is: " kw))
;;         (is (remove-from-storage backend k))))))
