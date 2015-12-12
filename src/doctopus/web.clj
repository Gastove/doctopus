(ns doctopus.web
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.walk :refer [keywordize-keys]]
            [compojure.core :refer [routes defroutes context GET POST ANY]]
            [doctopus.configuration :refer [server-config docs-uri-prefix]]
            [doctopus.db :as db]
            [doctopus.db.schema :as schema]
            [doctopus.web.jsapi :as jsapi]
            [doctopus.doctopus :refer [load-routes bootstrap-heads list-heads]]
            [doctopus.files.predicates :refer [html?]]
            [doctopus.template :as templates]
            [org.httpkit.server :as server]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.reload :as reload]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.stacktrace :as trace]
            [ring.util.response :as ring-response]
            [taoensso.timbre :as log])
  (:import [doctopus.doctopus Doctopus]))

(defn wrap-error-page
  "Utility ring handler for when Stuff goes Sideways; returns a 500 and an error
  page"
  [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           {:status 500
            :headers {"Content-Type" "text/html"}
            :body (slurp (io/resource "500.html"))}))))

(defn four-oh-four
  [route]
  (log/debug "404ing on route:" route)
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (format "<h3>Could not find a page matching %s </h3>" route)})

(defn wrap-route-not-found
  [handler]
  (fn [request]
    (if-let [response (handler request)]
      response
      (four-oh-four (:uri request)))))

(defn wrap-trim-trailing-slash
  "Compojure can't use regex; chomp a trailing slash before calling the handler."
  [handler]
  (fn [request]
    (let [uri (:uri request)
          new-uri (if (and (not (= "/" uri))
                           (re-find #"/$" uri))
                    (subs uri 0 (dec (count uri)))
                    uri)]
      (handler (assoc request :uri new-uri)))))

(defn serve-json
  "Turns a clojure data-structure into JSON and serves it with the correct
   content-type"
  [data]
  (-> data
      (json/generate-string)
      (ring-response/response)
      (ring-response/content-type "application/json")))

(defn serve-html
  "Just returns html, with the content-type set correctly"
  [html]
  (-> html
      (ring-response/response)
      (ring-response/content-type "text/html")))

;; A map from string-to-look-for to value-to-sub-with, representing global
;; configuration values that Heads will use to parse commands for their
;; tentacles. Currently hard-coded; could eventually be moved to config files if
;; we can every think of a reason why we'd want that.
(def substitutions {"$URL_ROOT"  (:ip (server-config))})

;; Let's start bootstrapping!
(def doctopus (Doctopus. (server-config) substitutions))

(defn serve-index
  [_]
  (serve-html (templates/index doctopus)))

(defn serve-head
  [params]
  (serve-html (templates/head-page (get-in params [:route-params :head-name]))))

(defn serve-all-heads
  [_]
  (serve-html (templates/heads-list doctopus)))

(defn serve-search-results
  [request]
  (let [query (:query-params request)
        {:keys [tentacle-name tentacle-local]} query]
    (serve-json {:ok true
                 :tentacle-name tentacle-name
                 :tentacle-local tentacle-local
                 :results [{:title "some document"
                            :snippet "beep boop this is a piece of snippet text"
                            :url "/some/url"}
                           {:title "some non-contextualized document"
                            :url "/some/other/thing"}
                           {:title "some other document"
                            :snippet "this is some other piece of snippet text for yer context"
                            :url "/some/other/url"}]})))

(def doctopus-routes
  (routes
   (GET "/" [] serve-index)
   (GET "/index.html" [] serve-index)
   ;(GET "/admin/*" [] serve-admin)
   (GET "/heads" [] serve-all-heads)
   (GET "/heads/:head-name" [head-name] serve-head)
   ;(GET "/tentacles" [] serve-all-tentacles)
   ;(GET "/tentacles/:tentacle-name" [tentacle-name] serve-tentacle)
   (GET "/search" [] serve-search-results)
   (context "/jsapi/v1" [] jsapi/jsapi-routes)))

(defn- get-tentacle-from-uri
  [request-uri]
  (let [pieces (re-find #"/docs/([^/]+)/.+" request-uri)]
    (if pieces (second pieces) nil)))

(defn wrap-omnibar-transform
  [handler]
  (fn [request]
    (let [response (handler request)
          body (:body response)
          tentacle-name (get-tentacle-from-uri (:uri request))]
      (if (and tentacle-name body (html? body))
        (assoc response :body (templates/add-omnibar body {:tentacle-name tentacle-name}))
        response))))

(def doctopus-ring-defaults
  (-> site-defaults
      (assoc-in [:static :resources] "public")))

(defn create-application
  [app-handlers]
  (-> (wrap-defaults app-handlers doctopus-ring-defaults)
      (wrap-trim-trailing-slash)
      (wrap-json-body)
      (wrap-omnibar-transform)
      (wrap-route-not-found)
      (reload/wrap-reload)
      ((if (= (:env (server-config)) :production)
         wrap-error-page
         trace/wrap-stacktrace))))

(defn parse-env []
  (case (:nomad/environment (server-config))
    ["dev" "travis"] :test
    ["prod"] :main
    :test))

;; # Http Server
;; This is what lifts the whole beast off the ground. Reads its configs out of
;; resources/configuration.edn
(defn -main
  []
  (let [{:keys [port]} (server-config)
        env (parse-env)
        ;; Building routes here to avoid loading the full doctopus stack until
        ;; app launch.
        application-routes (routes
                            (context (str "/" docs-uri-prefix) [] (load-routes doctopus))
                            doctopus-routes)
        application (create-application application-routes)]
    (log/info "Checking DB is set up...")
    (schema/bootstrap env)
    (log/info "Bootstrapping heads")
    (bootstrap-heads doctopus)
    (log/info "Starting HTTP server on port" port)
    (server/run-server application {:port port})))
