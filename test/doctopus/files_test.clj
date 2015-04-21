(ns doctopus.files-test
  (:require [doctopus.files :refer :all]
            [doctopus.test-utilities :refer :all]
            [clojure.test :refer :all]))

(deftest test-truncate-str
  (testing "Can we truncate one path by removing another path from the front?"
    (let [fq "/foo/bar/baz/bing/bang/document.markdown"
          remove-str "/foo/bar/baz/bing/"
          result (truncate-str fq remove-str)]
      (is (= result "bang/document.markdown")))))
