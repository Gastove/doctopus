(ns doctopus.shell-test
  (:require [doctopus.shell :refer :all]
            [clojure.test :refer :all]))

(deftest make-html-test
  (testing "Can we run a command that takes no arguments?"
    (let [expected "\n"
          result (make-html "echo")]
      (is (= (:out result) expected))))

  (testing "Can we run a command that takes arguments, but specifies no dir?"
    (let [expected "biff baz\n"
          result (make-html "echo" ["biff" "baz"])]
      (is (= (:out result) expected))))

  (testing "Can we supply a command, args, and a directory to work from?"
    (let [expected "biff boom baz\n"
          result (make-html "echo" ["biff" "boom" "baz"] "/")]
      (is (= (:out result) expected)))))
