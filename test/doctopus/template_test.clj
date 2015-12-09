(ns doctopus.template-test
  (:require [doctopus.template :refer :all]
            [clojure.test :refer :all]
            [doctopus.doctopus.head :refer :all]
            [doctopus.doctopus.tentacle :refer :all]
            [net.cgrand.enlive-html :as enlive]))

(deftest template
  (testing "add-frame injects an iframe in the body element"
    (is (not (nil? (re-find #"\<body\>\<iframe.*?\>\</body\>"
                            (add-frame "<body></body>")))))))
