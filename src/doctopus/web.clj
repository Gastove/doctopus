(ns doctopus.web
  (:require [bidi.ring :as bidi]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [doctopus.configuration :refer [server-config]]
            [org.httpkit.server :as server]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.reload :as reload]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.stacktrace :as trace]
            [ring.util.response :as ring-response]
            [taoensso.timbre :as log]))

(defn wrap-error-page [handler]
  "Utility ring handler for when Stuff goes Sideways; returns a 500 and an error
  page"
  (fn [req]
    (try (handler req)
         (catch Exception e
           {:status 500
            :headers {"Content-Type" "text/html"}
            :body (slurp (io/resource "500.html"))}))))

(defn four-oh-four
  [req]
  (log/debug "404ing on request:" req)
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (format "<h3>Could not find a page matching %s </h3>" (get-in req [:params :ext]))})

(defn serve-index
  "Just returns our index.html, with the content-type set correctly"
  [_]
  (-> (slurp (io/resource "index.html"))
      (ring-response/response)
      (ring-response/content-type "text/html")))

;; Bidi routes are defined as nested sets of ""
(def routes ["/" {""           {:get serve-index}
                  "index.html" {:get serve-index}
                  [:ext]       four-oh-four}])

(def application-handlers
  (bidi/make-handler routes))

(def application
  (-> (wrap-defaults application-handlers site-defaults)
      (reload/wrap-reload)
      ((if (= (:env (server-config)) :production)
         wrap-error-page
         trace/wrap-stacktrace))))

;; # Http Server
;; This is what lifts the whole beast off the ground. Reads its configs out of
;; resources/configuration.edn
(defn -main
  []
  (let [{:keys [port]} (server-config)]
    (log/info "Starting HTTP server on port" port)
    (server/run-server application {:port port})))
