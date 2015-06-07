(ns doctopus.template
  (:require [doctopus.doctopus :refer [list-heads list-tentacles]]
            [net.cgrand.enlive-html :as enlive :refer [deftemplate defsnippet]]
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

(defn- prepend-frame
  "injects the Doctopus iframe into the first body element of the given HTML"
  [main frame]
  (enlive/sniptest main [[:body enlive/first-of-type]] (enlive/prepend frame)))

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

(defn add-tentacle
  "creates the page with form for adding a Doctopus head"
  []
  (html {:page "add-tentacle"
         :submit "/add-tentacle"
         :csrf csrf-token/*anti-forgery-token*}))

(defn add-head
  "creates the page with form for adding a Doctopus head"
  []
  (html {:page "add-head"
         :submit "/add-head"
         :csrf csrf-token/*anti-forgery-token*}))

(defn tentacle-context
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
  [head]
  (html {:page "head-page"
         :head head}))

(defn index
  [doctopus]
  (html {:page "index"
         :tentacles (map tentacle-context (list-tentacles doctopus))
         :heads (map head-context (list-heads doctopus))}))
