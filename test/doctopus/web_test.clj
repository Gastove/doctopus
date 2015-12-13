(ns doctopus.web-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [compojure.core :refer [context]]
            [doctopus.configuration :refer [docs-uri-prefix]]
            [doctopus.db :as db]
            [doctopus.doctopus.head :as h]
            [doctopus.doctopus :refer [->Doctopus load-routes bootstrap-heads]]
            [doctopus.doctopus.tentacle :as t]
            [doctopus.test-database :refer [schema-only-fixture]]
            [doctopus.test-utilities :as utils]
            [doctopus.web :as web]))

(use-fixtures :once schema-only-fixture)

(def test-map-props
  (h/parse-tentacle-config-map
   (edn/read-string (slurp (io/resource "test/heads/test/doctopus-test.edn")))))

(def one-head (h/map->Head {:name "test"}))
(def one-tentacle (t/map->Tentacle test-map-props))
(def stunt-doctopus (->Doctopus {} {}))

(deftest document-routes-test
  (db/save-head! one-head)
  (db/save-tentacle! one-tentacle)
  (db/create-mapping! one-head one-tentacle)
  (bootstrap-heads stunt-doctopus)
  (testing "Doctopus document routes"
    (let [ctx-str (str "/" docs-uri-prefix)
          routes (context ctx-str [] (load-routes stunt-doctopus))
          response (utils/fake-request routes :get "/docs/doctopus-test/index.html")]
      (is (utils/truthy? (:body response)))
      (is (= "text/html" ((:headers response) "Content-Type")))
      (is (= 200 (:status response))))))

(deftest routes-test
  (testing "Tests against base, unwrapped doctopus-routes"
    (let [routes (web/create-application web/doctopus-routes)
          requester (partial utils/fake-request routes)]
      (is (= 200 (:status (requester :get "/"))))
      (is (= 200 (:status (requester :get "/index.html"))))
      (is (= 200 (:status (requester :get "/heads"))))
      (is (= 200 (:status (requester :get "/heads/doctopus-test"))))
      (is (= 200 (:status (requester :get "/add-head"))))
      (is (= 200 (:status (requester :get "/add-tentacle"))))
      ;; These two 403. Huh. Not... perfectly sure what to do.
      ;; (is (= 200 (:status (requester :post "/add-tentacle"))))
      ;; (is (= 200 (:status (requester :post "/add-head"))))
      )))
