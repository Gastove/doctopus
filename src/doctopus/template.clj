(ns doctopus.template
  (:require [net.cgrand.enlive-html :as enlive :refer [deftemplate defsnippet]]
            [doctopus.configuration :refer [server-config]]
            [doctopus.doctopus :refer [list-heads list-tentacles]]
            [ring.util.anti-forgery :as csrf]))

(def root-url (or (:root-url (server-config)) "/"))

(defn- linkify
  [href text]
  [:a {:href href} text])

(defn- head-li
  [head]
  (let [head-name (:name head)]
    (enlive/html [:li (linkify (str "/heads/" head-name) (:name head-name))])))

(defn- head-option
  [head]
  (let [head-name (:name head)]
    (enlive/html [:option {:value head-name} head-name])))

(defn- tentacle-li
  [tentacle]
  (enlive/html [:li (linkify (:root tentacle) (:name tentacle))]))

(deftemplate base-template "templates/base.html"
  [body]
  [:body] (enlive/content body))

(defsnippet add-head-snippet "templates/add-head.html"
  [:form]
  []
  [:form] (enlive/set-attr :action "/add-head")
  [:#csrf] (enlive/html-content (csrf/anti-forgery-field)))

(defsnippet add-tentacle-snippet "templates/add-tentacle.html"
  [:form]
  [doctopus]
  [:form] (enlive/set-attr :action "/add-tentacle")
  [:select] (enlive/content (map head-option (list-heads doctopus)))
  [:#csrf] (enlive/html-content (csrf/anti-forgery-field)))

(defsnippet index-snippet "templates/index.html"
  [:#doctopus-main]
  [doctopus]
  [:#doctopus-heads] (enlive/content (map head-li (list-heads doctopus)))
  [:#doctopus-tentacles] (enlive/content (map tentacle-li (list-tentacles doctopus))))

(defsnippet frame-snippet "templates/frame.html"
  [:#doctopus-iframe]
  [tentacle]
  [:#doctopus-index] (enlive/set-attr :href root-url)
  [:#doctopus-project] (enlive/do->
                        (enlive/set-attr :href (:config-url tentacle))
                        (enlive/content
                         (str "To " (:name tentacle) " config"))))

(defn- iframe-html
  "constructs an iframe element with relevant src attribute"
  [tentacle]
  (enlive/html [:iframe {:src (str (:root tentacle) "/frame.html")}]))

(defn- prepend-frame
  [main frame]
  (enlive/sniptest main [:body] (enlive/prepend frame)))

(defn add-frame
  "given a string of HTML and a tentacle, returns a string with a Doctopus
   iframe inserted into its <body>"
  [html-str tentacle]
  (apply str (prepend-frame html-str (iframe-html tentacle))))

(defn html
  [body]
  (apply str (base-template body)))

(defn project-frame
  "given a tentacle, returns a string of HTML suitable for serving an iframe
   with doctopus navigation"
  [tentacle]
  (html (frame-snippet tentacle)))

(defn index
  "returns an HTML string for main doctopus navigation"
  [doctopus]
  (html (index-snippet doctopus)))

(defn add-head
  []
  (html (add-head-snippet)))

(defn add-tentacle
  []
  (html (add-tentacle-snippet)))

