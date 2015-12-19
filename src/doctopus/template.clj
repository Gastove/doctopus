(ns doctopus.template
  (:require [net.cgrand.enlive-html :as enlive :refer [deftemplate defsnippet]]
            [doctopus.configuration :refer [server-config]]
            [doctopus.doctopus :refer [list-heads list-tentacles list-tentacles-by-head]]
            [doctopus.doctopus.tentacle :refer [get-html-entrypoint]]
            [cheshire.core :as json]
            [ring.util.anti-forgery :as csrf]
            [ring.middleware.anti-forgery :as csrf-token]))

(defn- omnibar-html
  "constructions domnibar (the doctopus omnibar) html"
  []
  (enlive/html
    [:link {:rel "stylesheet" :href "/assets/styles/css/omni.css"}]
    [:div#doctopus-omnibar]
    [:script {:src "/assets/scripts/main.js" :type "application/javascript"}]))

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
  "Given an an html string and an enlive-compiled html to-append, insert at the
  end of the given element; defaults to :body"
  ([main to-append]
   (append-to-element main :body to-append))
  ([main element to-append]
   (add-to-element-with-fn main element to-append enlive/append)))

(defn- prepend-to-element
  "Given an an html string and an enlive-compiled html to-prepend, insert at the
  beginning of the given element; defaults to :body"
  ([main to-prepend]
   (prepend-to-element main :body to-prepend))
  ([main element to-prepend]
   (add-to-element-with-fn main element to-prepend enlive/prepend)))

(deftemplate base-template "templates/base.html"
  [context]
  [:#app-state] (enlive/content (json/generate-string context)))

(defn- html
  "wraps the given body in the base template"
  [context]
  (apply str (base-template context)))

(defn- app-context
  "generates an enlive-encoded html description map for app-state; a known
  element used to communicate context to the frontend cljs app on page load"
  [context]
  (enlive/html
    [:script#app-state {:type "application/json"} (json/generate-string context)]))

(defn add-omnibar
  "given a string of HTML, returns a string with a the Doctopus omnibar and
  associated assets inserted into the appropriate places."
  [html-str context]
  (-> html-str
      (prepend-to-element :body (omnibar-html))
      (prepend-to-element :head (omnibar-css))
      (append-to-element :head (app-context context))))

(defn- tentacle-context
  [tentacle]
  {:name (:name tentacle) :location (get-html-entrypoint tentacle)})

(defn head-context
  [head]
  (let [head-name (:name head)]
    {:name head-name :location (str "/heads/" head-name)}))

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

(defn tentacle-items
  [tentacles]
  [:ul
    (for [tentacle tentacles]
      (let [tentacle-name (:name tentacle)
            tentacle-location (:location tentacle)]
        ^{:key tentacle-name} [:li
                               [:a {:href tentacle-location} tentacle-name]]))])

(defn head-items
  [heads]
  [:ul
    (for [head heads]
      (let [head-name (:name head) head-location (:location head)]
        ^{:key head-name} [:li [:a {:href head-location} head-name]]))])

(defn- index-template
  [doctopus]
  (enlive/html
    [:main
     [:section.module
      [:div.sub-header
       [:h2 "Heads"]]
      (head-items (map head-context (list-heads doctopus)))]
     [:section.module
      [:div.sub-header
       [:h2 "Tentacles"]]
      (tentacle-items (map tentacle-context (list-tentacles doctopus)))]]))

(defn index
  [doctopus]
  (add-to-element-with-fn (html {}) :body (index-template doctopus) enlive/substitute))

(defn admin
  [doctopus]
  (html {:page "admin"
         :tentacles (list-tentacles doctopus)
         :heads (list-heads doctopus)
         :csrf csrf-token/*anti-forgery-token*}))
