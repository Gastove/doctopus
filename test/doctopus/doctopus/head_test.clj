(ns doctopus.doctopus.head-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [doctopus.db :as db]
            [doctopus.doctopus.head :refer :all]
            [doctopus.doctopus.tentacle :as t]
            [doctopus.storage :as storage]
            [doctopus.test-utilities :as utils]
            [doctopus.test-database :refer [database-fixture]])
  (:import [doctopus.doctopus.head Head]))

(use-fixtures :once database-fixture)

(def test-head (Head. "test"))

(def test-tentacle-props
  (edn/read-string (slurp (io/resource "test/heads/test/doctopus-test.edn"))))

(def one-tentacle (t/map->Tentacle test-tentacle-props))

(storage/set-backend! :temp-fs)

;; This test just needs re-writing -- doesn't do a damn thing we care about right
;; now
;; (deftest head-test
;; (db/save-head! test-head)
;; (db/save-tentacle! one-tentacle)
;; (db/create-mapping! test-head one-tentacle)
;;   (testing "Can we bootstrap a Head's tentacles?"
;;     (let [tentacles (list-tentacles test-head {})]
;;       (is (not (nil? tentacles)) "Should have tentacles now")
;;       (is (= "doctopus-test" (:name (first tentacles))))
;;       (is (= one-tentacle (first (tentacles)))))
;;     (utils/clean-up-test-html "doctopus-test")))

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
