(ns doctopus.web.jsapi
  (:require [clojure.walk :refer [keywordize-keys]]
            [compojure.core :refer [routes defroutes context
                                    GET POST PATCH PUT DELETE]]
            [doctopus.configuration :refer [server-config]]
            [doctopus.db :as db]
            [doctopus.doctopus :refer [list-heads list-tentacles list-tentacles-by-head]]
            [doctopus.template :refer [head-context]]
            [ring.util.response :as ring-response]
            [taoensso.timbre :as log]
            [cheshire.core :as json]
            [clojure.string :as str])
  (:import [doctopus.doctopus Doctopus]))

(def substitutions {"$URL_ROOT"  (:ip (server-config))})
(def doctopus (Doctopus. (server-config) substitutions))

(defn serve-json
  "Turns a clojure data-structure into JSON and serves it with the correct
   content-type"
  ([data] (serve-json data 200))
  ([data status]
   (-> data
       (json/generate-string)
       (ring-response/response)
       (ring-response/status status)
       (ring-response/content-type "application/json"))))

(defn four-oh-four
  [request]
  (log/debug (str "404 on uri: " (:uri request)))
  (serve-json {:error "resource not found"} 404))

(defn get-route-descriptions
  [_]
  (serve-json {:routes {:heads {:uri "/jsapi/v1/heads"}
                        :tentacles {:uri "/jsapi/v1/tentacles"}}}))

(defn get-all-heads
  [_]
  (serve-json {:heads (map head-context (list-heads doctopus))}))

(defn get-tentacles-for-head
  [request]
  (let [head-name (get-in request [:route-params :head-name])]
    (if-let [head (db/get-head head-name)]
      (serve-json (assoc head :tentacles (list-tentacles-by-head doctopus head-name)))
      (four-oh-four request))))

(defn add-head
  [request]
  (let [head-name (get-in request [:body "name"])]
    (do
      (db/save-head! {:name head-name})
      (serve-json (db/get-head head-name) 201))))

(defn get-head
  [request]
  (if-let [head (db/get-head (get-in request [:route-params :head-name]))]
    (serve-json head)
    (four-oh-four request)))

(defn update-head
  [request]
  ;; TODO this should require a full update
  (if-let [head-name (get-in request [:route-params :head-name])]
    (do
      (if-not (empty? (get-head (:name head-name)))
        (db/update-head! (keywordize-keys (:body request)))
        (serve-json (db/get-head head-name) 200)))
    (four-oh-four request)))

(defn replace-head
  [request]
  (update-head request))

(defn delete-head
  [request]
  (let [head-name (get-in request [:route-params :head-name])]
    (if-not (nil? (db/get-head head-name))
      (do
        (db/delete-head! head-name)
        {:status 204})
      (four-oh-four request))))

(defn- get-all-tentacles
  [_]
  (serve-json {:tentacles (list-tentacles doctopus)}))

(defn add-tentacle
  [request]
  (let [params (keywordize-keys (:body request))
        tentacle {:name            (:name params)
                  :html-commands   (str/split-lines (:command params))
                  :source-location (:source params)}
        head (db/get-head (:head params))]
    (db/save-tentacle! tentacle)
    (db/create-mapping! head tentacle)
    (serve-json tentacle 201)))

(defn get-tentacle
  [request]
  (if-let [tentacle (db/get-tentacle (get-in request [:route-params :tentacle-name]))]
    (serve-json tentacle)
    (four-oh-four request)))

(defn update-tentacle
  [request]
  ;; TODO this should require a full update
  (if-let [tentacle-name (get-in request [:route-params :tentacle-name])]
    (do
      (if-not (empty? (db/get-tentacle tentacle-name))
        (db/update-tentacle! (keywordize-keys (:body request)))
        (serve-json (db/get-tentacle tentacle-name) 200)))
    (four-oh-four request)))

(defn replace-tentacle
  [request]
  (update-tentacle request))

(defn delete-tentacle
  [request]
  (let [tentacle-name (get-in request [:route-params :tentacle-name])]
    (if-not (nil? (db/get-tentacle tentacle-name))
      (do
        (db/delete-tentacle! tentacle-name)
        {:status 204})
      (four-oh-four request))))

(defroutes jsapi-routes
  (routes
    (GET "/" [] get-route-descriptions)
    (context "/heads" []
      (GET "/" [] get-all-heads)
      (POST "/" [] add-head)
      (context "/:head-name" [head-name]
        (GET "/" [head-name] get-head)
        (GET "/tentacles" [head-name] get-tentacles-for-head)
        (PUT "/" [head-name] replace-head)
        (PATCH "/" [head-name] update-head)
        (DELETE "/" [head-name] delete-head)))
    (context "/tentacles" []
      (GET "/" [] get-all-tentacles)
      (POST "/" [] add-tentacle)
      (context "/:tentacle-name" [tentacle-name]
        (GET "/" [tentacle-name] get-tentacle)
        (PUT "/" [tentacle-name] replace-tentacle)
        (PATCH "/" [tentacle-name] update-tentacle)
        (DELETE "/" [tentacle-name] delete-tentacle)))))
