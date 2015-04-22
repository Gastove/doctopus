(ns doctopus.storage.storage-impls.permanent-fs-test
  (:require [clojure.test :refer :all]
            [doctopus.files :as files]
            [doctopus.storage.storage-impls.permanent-fs :refer :all]
            [me.raynes.fs :as fs]))

(def test-vec (files/read-html "resources/test/html"))

(deftest permanent-fs-test
  (testing "Can we save to the permanent filesystem?"
    (save-fn ))
  (testing "Can we load from the permanent filesystem?")
  (testing "Can we remove from the permanent filesystem?"))
