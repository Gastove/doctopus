(ns doctopus.db-test
  (:require [doctopus.db.core :refer :all]
            [doctopus.db.schema :refer :all]
            [clojure.test :refer :all]))

(deftest db
  (testing "->kebab-keys transforms all keys to kebab-case"
    (is (= (#'doctopus.db.core/->kebab-keys {:key_name "lol"}) {:key-name "lol"}))
    (is (= (#'doctopus.db.core/->kebab-keys {:key_name "lol" :another_key "heh"})
           {:key-name "lol" :another-key "heh"}))
    (is (= (#'doctopus.db.core/->kebab-keys
            {:key_name "lol" :another_key "heh" :untouched-key "laffo"})
           {:key-name "lol" :another-key "heh" :untouched-key "laffo"})))
  (testing "->snake-keys transforms all keys to snake-case"
    (is (= (#'doctopus.db.core/->snake-keys {:key-name "lol"}) {:key_name "lol"}))
    (is (= (#'doctopus.db.core/->snake-keys {:key-name "lol" :another-key "heh"})
           {:key_name "lol" :another_key "heh"}))
    (is (= (#'doctopus.db.core/->snake-keys
            {:key-name "lol" :another-key "heh" :untouched_key "laffo"})
           {:key_name "lol" :another_key "heh" :untouched_key "laffo"})))
  (testing "add-updated adds updated and created fields"
    (is (= (#'doctopus.db.core/add-updated {} "right-now")
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
