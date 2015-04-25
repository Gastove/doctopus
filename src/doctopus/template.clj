(ns doctopus.template
  (:require [doctopus.doctopus :refer [list-heads list-tentacles]]
   [net.cgrand.enlive-html :as enlive :refer [deftemplate defsnippet]]
            [doctopus.configuration :refer [server-config]]
            [doctopus.doctopus :refer [list-heads list-tentacles list-tentacles-by-head]]
            [doctopus.doctopus.tentacle :refer [get-html-entrypoint]]
            [ring.util.anti-forgery :as csrf]))

(defn- make-anchor
  "given a uri and text, construct an anchor element"
  [href text]
  [:a {:href href} text])

(defn- head-li
  "constructs a list item with head link"
  [head]
  (let [head-name (:name head)]
    (enlive/html [:li (make-anchor (str "/heads/" head-name) head-name)])))

(defn- head-option
  "constructs an option element with head"
  [head]
  (let [head-name (:name head)]
    (enlive/html [:option {:value head-name} head-name])))

(defn- tentacle-li
  "constructs a list item with tentacle link"
  [tentacle]
  (let [tentacle-name (:name tentacle)]
    (enlive/html
     [:li (make-anchor (get-html-entrypoint tentacle) tentacle-name)])))

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
  [body]
  [:h1] (enlive/wrap :a)
  [[:a enlive/first-of-type]] (enlive/set-attr :href "/" :target "_parent")
  [:body] (enlive/content body))

(defsnippet index-snippet "templates/index.html"
  [:#doctopus-main]
  [doctopus]
  [:#doctopus-heads] (enlive/content (map head-li (list-heads doctopus)))
  [:#doctopus-tentacles] (enlive/content
                          (map tentacle-li
                               (flatten (list-tentacles doctopus)))))

(defsnippet head-snippet "templates/head.html"
  [:#doctopus-main]
  [head-name doctopus]
  [:#doctopus-head-name] (enlive/content head-name)
  [:#doctopus-tentacles] (enlive/content
                          (map tentacle-li
                               (flatten
                                (list-tentacles-by-head doctopus head-name)))))

(defsnippet heads-list-snippet "templates/head-list.html"
  [:#doctopus-main]
  [head-list]
  [:#doctopus-heads] (enlive/content (map head-li head-list)))

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

(defn- html
  "wraps the given body in the base template"
  [body]
  (apply str (base-template body)))

(defn index
  "returns an HTML string for main doctopus navigation"
  [doctopus]
  (html (index-snippet doctopus)))

(defn project-frame
  "returns a string of HTML suitable for serving an iframe with doctopus
   navigation"
  []
  (base-template ""))

(defn add-frame
  "given a string of HTML and a tentacle, returns a string with a Doctopus
   iframe inserted into its body"
  [html-str]
  (apply str (prepend-frame html-str (iframe-html))))

(defn heads-list
  "creates a page for listing all heads"
  [doctopus]
  (html (heads-list-snippet (list-heads doctopus))))

(defn head-page
  "creates the page for a Doctopus head"
  [head-name doctopus]
  (html (head-snippet head-name doctopus)))

(defn add-head
  "creates the page with form for adding a Doctopus head"
  []
  (html (add-head-snippet)))

(defn add-tentacle
  "creates the page with form for adding a Doctopus tentacle"
  [doctopus]
  (html (add-tentacle-snippet doctopus)))
