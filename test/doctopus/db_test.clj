(ns doctopus.db-test
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [doctopus.configuration :refer [server-config]]
            [doctopus.db :refer :all]
            [doctopus.db.schema :refer :all]
            [doctopus.test-utilities :as utils]
            [korma.core :as sql]
            [korma.db :refer [defdb postgres]]))

(defdb test-db (postgres
                (select-keys (get-in (server-config) [:database :test])
                             [:db :user :password :host :port])))

(defn- truncate!
  [table-name]
  (let [sql-string (format "TRUNCATE %s CASCADE" table-name)]
    (do-sql-with-logging! sql-string :test)))

(defn database-fixture
  [f]
  (bootstrap :test)
  (f)
  (doseq [n (keys table-name->schema)] (truncate! n)))

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
    (testing "Can we save head-tentacle mappings?"
      (doall (map #(create-mapping! % %2) mock-heads mock-tentacles))
      (is (= 5 (count (get-all-mappings)))))
    (doseq [head mock-heads
            tentacle mock-tentacles]
      (delete-head! head)
      (delete-tentacle! tentacle))))
