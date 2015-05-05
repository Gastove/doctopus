(ns doctopus.db-test
  (:require [clojure.test :refer :all]
            [doctopus
             [db :refer :all]
             [test-utilities :as utils]]
            [doctopus.db.schema :refer :all]
            [doctopus.test-database :refer [database-fixture]]))

(use-fixtures :once database-fixture)

(deftest db-name-and-data-transforms
  (testing "->kebab-keys transforms all keys to kebab-case"
    (is (= (#'doctopus.db/->kebab-keys {:key_name "lol"}) {:key-name "lol"}))
    (is (= (#'doctopus.db/->kebab-keys {:key_name "lol" :another_key "heh"})
           {:key-name "lol" :another-key "heh"}))
    (is (= (#'doctopus.db/->kebab-keys
            {:key_name "lol" :another_key "heh" :untouched-key "laffo"})
           {:key-name "lol" :another-key "heh" :untouched-key "laffo"})))
  (testing "->snake-keys transforms all keys to snake-case"
    (is (= (#'doctopus.db/->snake-keys {:key-name "lol"}) {:key_name "lol"}))
    (is (= (#'doctopus.db/->snake-keys {:key-name "lol" :another-key "heh"})
           {:key_name "lol" :another_key "heh"}))
    (is (= (#'doctopus.db/->snake-keys
            {:key-name "lol" :another-key "heh" :untouched_key "laffo"})
           {:key_name "lol" :another_key "heh" :untouched_key "laffo"})))
  (testing "add-updated adds updated and created fields"
    (is (= (#'doctopus.db/add-updated {} "right-now")
           {:updated "right-now"}))))

(deftest schema
  (testing "get-subname fills in defaults"
    (is (= (#'doctopus.db.schema/get-subname {}) "//localhost:5432/doctopus"))
    (is (= (#'doctopus.db.schema/get-subname {:host "meow"})
           "//meow:5432/doctopus"))
    (is (= (#'doctopus.db.schema/get-subname {:host "meow" :db "cats"})
           "//meow:5432/cats"))
    (is (= (#'doctopus.db.schema/get-subname {:host "meow" :db "cats" :port 31})
           "//meow:31/cats"))))

(deftest db-joins-and-lookups
  (let [tentacle-mocker #(utils/mock-data :tentacle nil)
        mock-tentacles (take 5 (repeatedly tentacle-mocker))
        head-mocker #(utils/mock-data :head nil)
        mock-heads (take 5 (repeatedly head-mocker))]
    (testing "Can we save heads?"
      (doseq [head mock-heads] (save-head! head))
      (is (= 5 (count (get-all-heads)))))
    (testing "Can we save tentacles?"
      (doseq [tentacle mock-tentacles] (save-tentacle! tentacle))
      (is (= 5 (count (get-all-tentacles)))))

    ;; For these next two tests, we'll want to be able to reference the first
    ;; head and the first tentacle consistently and easily
    (let [first-head (first mock-heads)
          first-tent (first mock-tentacles)]
      (testing "Can we save head-tentacle mappings?"
        (doseq [tent mock-tentacles] (create-mapping! first-head tent))
        (doseq [head mock-heads] (create-mapping! head first-tent))
        ;; first-head and first-tentacle only need a single mapping
        (is (= 9 (count (get-all-mappings)))))
      (testing "Can we get the tentacles for a head, using the mapping?"
        (is (= 5 (count (get-tentacles-for-head first-head)))))
      (testing "Can we get the heads for a tentacle, using the mapping?"
        (is (= 5 (count (get-heads-for-tentacle first-tent))))))
    (testing "Can we delete heads and tentacles?"
      (doall (for [head mock-heads] (delete-head! head)))
      (doall (for [tent mock-tentacles] (delete-tentacle! tent)))
      (is (= 0 (count (get-all-heads))))
      (is (= 0 (count (get-all-tentacles)))))
    (testing "Did the mappings table get updated correct?"
      (is (= 0 (count (get-all-mappings)))))))
