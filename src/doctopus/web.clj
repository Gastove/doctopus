(ns doctopus.web
  (:require [bidi.ring :as bidi]
            [clojure.java.io :as io]
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

(defn serve-index
  [_]
  (-> (slurp (io/resource "index.html"))
      (ring-response/response)
      (ring-response/content-type "text/html")))

(def routes
  (bidi/make-handler ["/" {"" {:get serve-index}}]))

(def application
  (-> (wrap-defaults routes site-defaults)
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
