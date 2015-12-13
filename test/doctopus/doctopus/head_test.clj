(ns doctopus.doctopus.head-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [doctopus.db :as db]
            [doctopus.db.schema :as schema]
            [doctopus.doctopus.head :refer :all]
            [doctopus.doctopus.tentacle :as t]
            [doctopus.test-database :refer [schema-only-fixture]]
            [doctopus.test-utilities :as utils])
  (:import [doctopus.doctopus.head Head]))

(use-fixtures :once schema-only-fixture)

(def test-map-props
  (parse-tentacle-config-map
   (edn/read-string (slurp (io/resource "test/heads/test/doctopus-test.edn")))))

(def one-head (map->Head {:name "test"}))
(def one-tentacle (t/map->Tentacle test-map-props))

(deftest head-test
  (db/save-head! one-head)
  (db/save-tentacle! one-tentacle)
  (db/create-mapping! one-head one-tentacle)
  (testing "Can we bootstrap tentacles"
    (bootstrap-tentacles one-head)
    (is (< 0 (t/count-records one-tentacle)) "There should be HTML for this tentacle")))

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
