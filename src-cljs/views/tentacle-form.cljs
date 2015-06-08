(ns doctopus.views.tentacle-form
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [doctopus.util :as util :refer [get-value http-ok? redirect-to]]
            [reagent.core :as reagent :refer [atom]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def form-data (atom {}))

(defn- submit-button
  [submit-url]
  [:input.btn.medium.secondary {:type "button"
                                :value "Save"
                                :on-click #(validate-form submit-url)}])

(defn- head-selector
  [heads]
  [:select#head-selector
    (for [head heads]
      (let [head-name (:name head)]
        ^{:key head-name} [:option {:value head-name} head-name]))])

(defn tentacle-form
  [{:keys [csrf submit-url original-name heads] :or {original-name ""}}]
  (do
      (swap! form-data assoc :original-name original-name
                             :name original-name
                             :csrf csrf)
      (fn []
        [:form.main
         [:fieldset
          [:div
           [:label {:for "head-selector"} "Belonging to:"]
           [head-selector heads]]
          [:div
           [:label {:for "tentacle-name"} "Tentacle Name"]
           [tentacle-input]]
          [:div
           [:label {:for "tentacle-source"} "Tentacle Source"]
           [tentacle-source-input]]
          [:div
           [:label {:for "tentacle-command"} "Tentacle Command"]
           [tentacle-command-input]]]
         [submit-button submit-url]])))
