(ns doctopus.template
  (:require [net.cgrand.enlive-html :as enlive]
            [doctopus.configuration :refer [server-config]]))

(def root-url (or (:root-url (server-config)) "/"))

(enlive/deftemplate frame-template "templates/frame.html"
  [tentacle]
  [:#doctopus-index] (enlive/set-attr :href root-url)
  [:#doctopus-project] (enlive/do->
                        (enlive/set-attr :href (:config-url tentacle))
                        (enlive/content
                         (str "To " (:name tentacle) " config"))))

(defn- iframe-html
  [tentacle]
  (enlive/html [:iframe {:src (str (:root tentacle) "/frame.html")}]))

(defn- prepend-frame
  [main frame]
  (enlive/sniptest main [:body] (enlive/prepend frame)))

(defn add-frame
  "given a string of HTML and a tentacle, returns a string with a Doctopus
   iframe inserted into its <body>"
  [html tentacle]
  (apply str (prepend-frame html (iframe-html tentacle))))

(defn project-frame
  [tentacle]
  (apply str (frame-template tentacle)))
