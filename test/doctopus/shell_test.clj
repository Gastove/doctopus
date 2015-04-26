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


(deftest injest-shell-vector-test
  (let [input-string "make -C docs/ html"
        input-vector ["npm i"
                       "node bin/mop.js"]
        single-vec-expected ["make" "-C" "docs/" "html"]
        vec-o-vecs-expected [["npm" "i"]
                             ["node" "bin/mop.js"]]]
    (testing "Can we correctly split strings?"
      (is (= (split input-string) single-vec-expected)
          "Can we split a single vector?")
      (is (= vec-o-vecs-expected (injest-shell-vector input-vector))
          "When we injest a vector, does it split correctly *and* preserve order?"))))
