(ns doctopus.template
  (:require [net.cgrand.enlive-html :as enlive :refer [deftemplate defsnippet]]
            [doctopus.configuration :refer [server-config]]
            [doctopus.doctopus :refer [list-heads list-tentacles list-tentacles-by-head]]
            [doctopus.doctopus.tentacle :refer [get-html-entrypoint]]
            [clojure.data.json :as json]
            [ring.util.anti-forgery :as csrf]
            [ring.middleware.anti-forgery :as csrf-token]))

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

(defn- add-to-element-with-fn
  [main element snippet f]
  (enlive/sniptest main [[element enlive/first-of-type]] (f snippet)))

(defn- append-to-element
  ([main to-append]
   (append-to-element main :body to-append))
  ([main element to-append]
   (add-to-element-with-fn main element to-append enlive/append)))

(defn- prepend-to-element
  ([main to-prepend]
   (prepend-to-element main :body to-prepend))
  ([main element to-prepend]
   (add-to-element-with-fn main element to-prepend enlive/prepend)))

(deftemplate base-template "templates/base.html"
  [context]
  [:h1] (enlive/wrap :a)
  [[:a enlive/first-of-type]] (enlive/set-attr :href "/" :target "_parent")
  [:#app-state] (enlive/content (json/write-str context)))

(defn- html
  "wraps the given body in the base template"
  [context]
  (apply str (base-template context)))

(defn- app-context
  [context]
  (enlive/html
    [:script#app-state {:type "application/javascript"} (json/write-str context)]))

(defn add-omnibar
  "given a string of HTML, returns a string with a the Doctopus omnibar inserted
  at the end of its body"
  [html-str context]
  (-> html-str
      (prepend-to-element :body (omnibar-html))
      (prepend-to-element :head (omnibar-css))
      (append-to-element :body (app-context context))))

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
