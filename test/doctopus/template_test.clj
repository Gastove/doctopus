(ns doctopus.template-test
  (:require [doctopus.template :refer :all]
            [clojure.test :refer :all]
            [doctopus.doctopus.head :refer :all]
            [doctopus.doctopus.tentacle :refer :all]
            [net.cgrand.enlive-html :as enlive]))

(deftest template
  (testing "make-anchor makes an anchor element"
    (is (= (make-anchor "/" "test") [:a {:href "/"} "test"])))
  (testing "add-frame injects an iframe in the body element"
    (is (not (nil? (re-find #"\<body\>\<iframe.*?\>\</body\>"
                            (add-frame "<body></body>"))))))
  (testing "head-li creates a list item with a head link"
    (is (= (head-li (map->Tentacle {:name "cat"}))
           (enlive/html [:li [:a {:href "/heads/cat"} "cat"]]))))
  (testing "head-li creates a list item with a head link"
    (is (= (head-option (map->Tentacle {:name "cat"}))
           (enlive/html [:option {:value "cat"} "cat"]))))
  (testing "tentacle-li creates a list item with a tentacle link"
    (is (= (tentacle-li (map->Tentacle {:name "cat" :entry-point "index.html"}))
           (enlive/html [:li [:a {:href "/docs/cat/index.html"} "cat"]])))))
