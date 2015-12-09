(ns doctopus.template
  (:require [net.cgrand.enlive-html :as enlive :refer [deftemplate defsnippet]]
            [doctopus.configuration :refer [server-config]]
            [doctopus.doctopus :refer [list-heads list-tentacles list-tentacles-by-head]]
            [doctopus.doctopus.tentacle :refer [get-html-entrypoint]]
            [clojure.data.json :as json]
            [ring.util.anti-forgery :as csrf]
            [ring.middleware.anti-forgery :as csrf-token]))

(defn- iframe-html
  "constructs an iframe element with relevant src attribute"
  []
  (enlive/html
   [:iframe {:src "/frame.html" :width "100%" :height "90" :frameBorder "0"}]))

(defn- omnibar-html
  "constructions domnibar (the doctopus omnibar) html"
  []
  (enlive/html
    [:link {:rel "stylesheet" :href "/assets/styles/css/omni.css"}]
    [:div#doctopus-omnibar]
    [:script {:src "/assets/scripts/omni.js" :type "application/javascript"}]))

(defn- omnibar-css
  "constructs a stylesheet reference for the domnibar"
  []
  (enlive/html
    [:script {:src "//use.typekit.net/txl6yqy.js" :type "application/javascript"}]
    [:script "try{Typekit.load();}catch(e){}"]
    [:link {:rel "stylesheet" :href "/assets/styles/css/omni.css"}]))

(defn- prepend-to-element
  ([main to-prepend]
   (prepend-to-element main :body to-prepend))
  ([main element to-prepend]
   (enlive/sniptest main [[element enlive/first-of-type]] (enlive/prepend to-prepend))))

(deftemplate base-template "templates/base.html"
  [context]
  [:h1] (enlive/wrap :a)
  [[:a enlive/first-of-type]] (enlive/set-attr :href "/" :target "_parent")
  [:#app-state] (enlive/content (json/write-str context)))

(defn- html
  "wraps the given body in the base template"
  [context]
  (apply str (base-template context)))

(defn project-frame
  "returns a string of HTML suitable for serving an iframe with doctopus
   navigation"
  []
  (base-template ""))

(defn add-frame
  "given a string of HTML, returns a string with a Doctopus iframe inserted into
   its body"
  [html-str]
  (apply str (prepend-frame html-str (iframe-html))))

(defn add-omnibar
  "given a string of HTML, returns a string with a the Doctopus omnibar inserted
  at the end of its body"
  [html-str]
  (apply str (prepend-to-element (prepend-to-element html-str :head (omnibar-css))
                                 :body (omnibar-html))))

(defn- tentacle-context
  [tentacle]
  {:name (:name tentacle) :location (get-html-entrypoint tentacle)})

(defn head-context
  [head]
  (let [head-name (:name head)]
    {:name head-name :location (str "/heads/" head-name)}))

(defn add-tentacle
  "creates the page with form for adding a Doctopus tentacle"
  [doctopus]
  (html {:page "add-tentacle"
         :submit "/add-tentacle"
         :heads (map head-context (list-heads doctopus))
         :tentacles (map tentacle-context (list-tentacles doctopus))
         :csrf csrf-token/*anti-forgery-token*}))

(defn add-head
  "creates the page with form for adding a Doctopus head"
  [doctopus]
  (html {:page "add-head"
         :submit-url "/add-head"
         :heads (map head-context (list-heads doctopus))
         :csrf csrf-token/*anti-forgery-token*}))

(defn heads-list
  [doctopus]
  (html {:page "heads-list"
         :heads (map head-context (list-heads doctopus))}))

(defn head-page
  [head-name]
  (html {:page "head-page"
         :submit-url "/update-head"
         :original-name head-name
         :csrf csrf-token/*anti-forgery-token*}))

(defn index
  [doctopus]
  (html {:page "index"
         :tentacles (map tentacle-context (list-tentacles doctopus))
         :heads (map head-context (list-heads doctopus))}))
