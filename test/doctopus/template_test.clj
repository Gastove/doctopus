(ns doctopus.template-test
  (:require [doctopus.template :refer :all]
            [clojure.test :refer :all]
            [doctopus.doctopus.head :refer :all]
            [doctopus.doctopus.tentacle :refer :all]
            [net.cgrand.enlive-html :as enlive]))

(deftest template
  (testing "append inserts html at end of selection"
    (is (= "<body><div>test</div><div>inserted</div></body>"
           (#'doctopus.template/append-to-element
             "<body><div>test</div></body>"
             :body
             (enlive/html [:div "inserted"])))))

  (testing "prepend inserts html at beginning of selection"
    (is (= "<body><div>inserted</div><div>test</div></body>"
           (#'doctopus.template/prepend-to-element
             "<body><div>test</div></body>"
             :body
             (enlive/html [:div "inserted"])))))

  (testing "add-omnibar inserts omnibar and context into existing html"
    (is (not (nil? (re-find #"omnibar"
                            (add-omnibar "<head></head><body></body>" {})))))
    (is (not (nil? (re-find #"muh-context"
                            (add-omnibar "<head></head><body></body>"
                                         {:muh-context true})))))))

