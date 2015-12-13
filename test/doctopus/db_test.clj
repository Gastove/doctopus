(ns doctopus.db-test
  (:require [clojure.test :refer :all]
            [doctopus
             [db :refer :all]
             [test-utilities :as utils]]
            [doctopus.db.jdbc :as jdbc]
            [doctopus.db.schema :refer :all]
            [doctopus.test-database :refer [schema-only-fixture truncate!]]))

(use-fixtures :once schema-only-fixture)

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
    (is (= (#'doctopus.db.jdbc/get-subname {}) "//localhost:5432/doctopus"))
    (is (= (#'doctopus.db.jdbc/get-subname {:host "meow"})
           "//meow:5432/doctopus"))
    (is (= (#'doctopus.db.jdbc/get-subname {:host "meow" :db "cats"})
           "//meow:5432/cats"))
    (is (= (#'doctopus.db.jdbc/get-subname {:host "meow" :db "cats" :port 31})
           "//meow:31/cats"))))

(deftest db-crud-joins-and-lookups
  ;; When we bootstrap the database, it finishes with some data in it. Gonna be
  ;; easier if we just pave that fresh for these tests
  (truncate! "heads")
  (truncate! "tentacles")
  (truncate! "head_tentacle_mappings")
  (truncate! "documents")
  (let [tentacle-mocker #(utils/mock-data :tentacle nil)
        mock-tentacles (take 5 (repeatedly tentacle-mocker))
        head-mocker #(utils/mock-data :head nil)
        mock-heads (take 5 (repeatedly head-mocker))
        ;; Documents have much more regular string formatting constraints, so
        ;; we'll make one, by hand.
        mock-doc {:name "doctopus_test"
                  :body "<p>I'm a paragraph and I'm <emph>OK</emph></p>"
                  :tentacle-name (:name (first mock-tentacles))
                  :mime-type "text/html"
                  :uri "index.html"}]

    ;; Let's start some testing:
    (testing "Can we save and retrieve heads?"
      (doseq [head mock-heads] (save-head! head))
      (is (= 5 (count (get-all-heads))) "We should have 5 saved heads"))

    (testing "Can we save and retrieve tentacles?"
      (doseq [tentacle mock-tentacles] (save-tentacle! tentacle))
      (is (= 5 (count (get-all-tentacles))) "We should have 5 saved tentacles"))

    (testing "Can we save documents?"
      (is (= 1 (save-document! mock-doc))
          "We should get back a single map representing the created doc"))

    (testing "Can we load a document by name? By URI? By tentacle?"
      (is (= 1 (count (get-document-by-name "doctopus_test")))
          "We should get one and only one tentacle")
      (is (= 1 (count (get-document-by-uri "index.html")))
          "Also, one tentacle.")
      (is (= 1 (count (get-all-documents-for-tentacle (first mock-tentacles))))
          "One again.")
      (is (= (get-document-by-name "doctopus_test")
             (get-document-by-uri "index.html")
             (get-all-documents-for-tentacle (first mock-tentacles)))
          "We should get the same tentacle, regardless of retrieval"))

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

    (testing "Can we delete heads and tentacles and their child data?"
      (doall (for [head mock-heads] (delete-head! head)))
      (doall (for [tent mock-tentacles] (delete-tentacle! tent)))
      (is (= 0 (count (get-all-heads))))
      (is (= 0 (count (get-all-tentacles)))))

    (testing "Did the mappings table get updated correct?"
      (is (= 0 (count (get-all-mappings)))))
    (testing "Did the mock document get deleted when it's tentacle was nuked?"
      (is (= 0 (count (get-all-documents-for-tentacle (first mock-tentacles))))))))
