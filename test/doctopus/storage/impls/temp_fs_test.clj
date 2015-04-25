(ns doctopus.storage.impls.temp-fs-test
  (:require [clojure.set :as set]
            [clojure.test :refer :all]
            [doctopus.files :as files]
            [doctopus.storage.impls.temp-fs :refer :all]
            [doctopus.test-utilities :as utils]
            [me.raynes.fs :as fs]))


;; (def test-pairs (files/read-html "resources/test/html"))
;; (def test-key (str (utils/make-today) "-doctopus-testing"))

(defn- thing-count
  "Brute-force counts the files in a dir"
  [dir]
  (count (apply set/union (map #(nth % 2) (fs/walk vector dir)))))

;; (deftest temp-fs-test
;;   (let [initial-count (thing-count (fs/file "resources/test/html"))]
;;     (testing "Can we save to the permanent filesystem?"
;;       (save-fn test-key test-pairs)
;;       (print initial-count)
;;       (binding [fs/*cwd* @temp-dir]
;;         (is (= initial-count (thing-count (fs/file test-key))))))
;;     (testing "Can we load from the permanent filesystem?"
;;       (is (= initial-count (count (load-fn test-key)))))
;;     (testing "Can we remove from the permanent filesystem?"
;;       (remove-fn test-key)
;;       (is (false? (fs/exists? (fs/file test-key)))))))
