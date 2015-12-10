(ns doctopus.web
  (:require [bidi.ring :as bidi :refer [->Resources]]
            [clojure.java.io :as io]
            [doctopus.configuration :refer [server-config]]
            [doctopus.doctopus :refer [load-routes bootstrap-heads list-heads]]
            [doctopus.template :as templates]
            [doctopus.files.predicates :refer [html?]]
            [doctopus.db :as db]
            [doctopus.db.schema :as schema]
            [org.httpkit.server :as server]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.reload :as reload]
            [ring.middleware.stacktrace :as trace]
            [ring.util.response :as ring-response]
            [taoensso.timbre :as log]
            [cheshire.core :as json]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.string :as str]
            [ring.middleware.anti-forgery :as csrf-token])
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

(defn serve-add-head-form
  [_]
  (serve-html (templates/add-head doctopus)))

(defn add-head
  [request]
  (let [head-name (get-in request [:body "name"])]
    (do
      (db/save-head! {:name head-name})
      (serve-json {:success-url (str "/heads/" head-name)}))))

(defn serve-head
  [params]
  (serve-html (templates/head-page (get-in params [:route-params :head-name]))))

(defn serve-all-heads
  [_]
  (serve-html (templates/heads-list doctopus)))

(defn serve-add-tentacle-form
  [_]
  (serve-html (templates/add-tentacle doctopus)))

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
                           {:title "some other document"
                            :snippet "this is some other piece of snipppet text for yer context"
                            :url "/some/other/url"}]})))

(defn add-tentacle
  [request]
  (let [params (keywordize-keys (:body request))
        tentacle {:name            (:name params)
                  :html-commands   (str/split-lines (:command params))
                  :source-location (:source params)}
        head (db/get-head (:head params))]
    (db/save-tentacle! tentacle)
    (db/create-mapping! head tentacle)
    (serve-json {:success-url (str "/tentacles/" (:name tentacle))})))

;; Bidi routes are defined as nested sets of ""
(defn generate-routes []
  ["/" {""             {:get serve-index}
        "index.html"   {:get serve-index}
        "assets"       (->Resources {:prefix "public/assets"})
        "heads"        {"/"           {:get serve-all-heads}
                        ""            {:get serve-all-heads}
                        [:head-name]  {:get serve-head}}
        "add-head"     {:get serve-add-head-form :post add-head}
        "add-tentacle" {:get serve-add-tentacle-form :post add-tentacle}
        "search"       {:get serve-search-results}
        "docs"         (load-routes doctopus)}])

(defn- get-tentacle-from-uri
  [request-uri]
  (let [pieces (re-find #"/docs/([^/]+)/.+" request-uri)]
    (if pieces (second pieces) nil)))

(defn generate-application-handlers
  [routes]
  (bidi/make-handler routes))

(defn wrap-omnibar-transform
  [handler]
  (fn [request]
    (let [response (handler request)
          file (:body response)
          tentacle-name (get-tentacle-from-uri (:uri request))]
      (if (and tentacle-name file (html? file))
        (assoc response :body (templates/add-omnibar (slurp file) {:tentacle-name tentacle-name}))
        response))))

(defn create-application
  [app-handlers]
  (-> (wrap-defaults app-handlers site-defaults)
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
        routes (generate-routes)
        handlers (generate-application-handlers routes)
        application (create-application handlers)]
    (log/info "Checking DB is set up...")
    (schema/bootstrap env)
    (log/info "Bootstrapping heads")
    (bootstrap-heads doctopus)
    (log/info "Starting HTTP server on port" port)
    (server/run-server application {:port port})))
