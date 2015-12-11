(ns doctopus.doctopus.head-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [doctopus.db :as db]
            [doctopus.doctopus.head :refer :all]
            [doctopus.doctopus.tentacle :as t]
            [doctopus.storage :as storage]
            [doctopus.test-database :refer [database-fixture]]
            [doctopus.test-utilities :as utils])
  (:import [doctopus.doctopus.head Head]))

(use-fixtures :once database-fixture)
(def one-head (map->Head {:name "main"}))
;; (storage/set-backend! :temp-fs)

(deftest head-test
  (db/save-head! one-head)
  (testing "Can we bootstrap a Head's tentacles?"
    (let [head (map->Head (db/get-head "main"))
          tentacles (list-tentacles head {})]
      (is (not (nil? tentacles)) "Should have tentacles now")
      (is (= "doctopus" (:name (first tentacles)))
          "Should have a tentacle with a known name"))
    (utils/clean-up-test-html "doctopus-test")))

(deftest injest-shell-strings-test
  (let [input-single-vec ["make -C docs/ html"]
        input-multi-vector ["npm i"
                            "node bin/mop.js"]
        single-vec-expected [["make" "-C" "docs/" "html"]]
        vec-o-vecs-expected [["npm" "i"]
                             ["node" "bin/mop.js"]]]
    (testing "Can we correctly split strings?"
      (is (= (injest-shell-strings input-single-vec) single-vec-expected)
          "Can we split a single vector?")
      (is (= (injest-shell-strings input-multi-vector) vec-o-vecs-expected)
          "When we injest a vector, does it split correctly *and* preserve order?"))))

(deftest substitutions-test
  (let [input-vec ["do" "re" "mi"]
        expected-vec ["do" "penguin" "mi"]
        substitutions-map {"re" "penguin"}]
    (is (= expected-vec (perform-substitutions! substitutions-map input-vec))
        "Should swap 're' for 'penguin")
    (is (= input-vec (perform-substitutions! {"nope!" "you should never see me"} input-vec))
        "Shouldn't change a thing.")))
