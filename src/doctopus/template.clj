(ns doctopus.template
  (:require [doctopus.doctopus :refer [list-heads list-tentacles]]
   [net.cgrand.enlive-html :as enlive :refer [deftemplate defsnippet]]
            [doctopus.configuration :refer [server-config]]
            [doctopus.doctopus :refer [list-heads list-tentacles list-tentacles-by-head]]
            [doctopus.doctopus.tentacle :refer [get-html-entrypoint]]
            [ring.util.anti-forgery :as csrf]))

(def root-url (or (:root-url (server-config)) "/"))

(defn- linkify
  [href text]
  [:a {:href href} text])

(defn- head-li
  [head]
  (let [head-name (:name head)]
    (enlive/html [:li (linkify (str "/heads/" head-name) head-name)])))

(defn- head-option
  [head]
  (let [head-name (:name head)]
    (enlive/html [:option {:value head-name} head-name])))

(defn- tentacle-li
  [tentacle]
  (let [tentacle-name (:name tentacle)]
    (enlive/html [:li (linkify (get-html-entrypoint tentacle) tentacle-name)])))

(defsnippet head-snippet "templates/head.html"
  [:#doctopus-main]
  [head-name doctopus]
  [:#doctopus-head-name] (enlive/content head-name)
  [:#doctopus-tentacles] (enlive/content (map tentacle-li (flatten (list-tentacles-by-head doctopus head-name)))))

(deftemplate base-template "templates/base.html"
  [body]
  [:h1] (enlive/wrap :a)
  [[:a enlive/first-of-type]] (enlive/set-attr :href "/")
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
  [:#doctopus-tentacles] (enlive/content (map tentacle-li (flatten (list-tentacles doctopus)))))

(defn- iframe-html
  "constructs an iframe element with relevant src attribute"
  []
  (enlive/html [:iframe {:src "/frame.html" :width "100%" :height "90" :frameBorder "0"}]))

(defn- prepend-frame
  [main frame]
  (enlive/sniptest main [[:body enlive/first-of-type]] (enlive/prepend frame)))

(defn add-frame
  "given a string of HTML and a tentacle, returns a string with a Doctopus
   iframe inserted into its body"
  [html-str]
  (apply str (prepend-frame html-str (iframe-html))))

(defn html
  [body]
  (apply str (base-template body)))

(defn project-frame
  "returns a string of HTML suitable for serving an iframe with doctopus
   navigation"
  []
  (base-template ""))

(defn index
  "returns an HTML string for main doctopus navigation"
  [doctopus]
  (html (index-snippet doctopus)))

(defn head-page
  [head-name doctopus]
  (html (head-snippet head-name doctopus)))

(defn add-head
  []
  (html (add-head-snippet)))

(defn add-tentacle
  [doctopus]
  (html (add-tentacle-snippet doctopus)))
