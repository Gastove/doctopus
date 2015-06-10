(ns doctopus.views.tentacle-form
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [doctopus.util :refer [get-value http-ok? redirect-to]]
            [doctopus.views.common :refer [button]]
            [reagent.core :as reagent :refer [atom]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def form-data (atom {}))
(def csrf-token (atom ""))

(defn- submit-form
  [data submit-url]
  (go
   (let [response (<! (http/post submit-url
                                 {:json-params data
                                  :headers {"X-CSRF-Token" @csrf-token}}))]
     (if (http-ok? (:status response))
       (redirect-to (:success-url response))
       (show-form-error (:error response))))))

(defn- validate-form
  [submit-url]
  (submit-form @form-data submit-url))

(defn- head-selector
  [heads]
  [:select#head-selector
    (for [head heads]
      (let [head-name (:name head)]
        ^{:key head-name} [:option {:value head-name} head-name]))])

(defn- input
  [id initial-value on-change]
  [(keyword (str "input#" id)) {:type "text"
                                :value initial-value
                                :on-change on-change}])

(defn- tentacle-input
  []
  (input "tentacle-name" (:name @form-data)
         #(swap! form-data assoc :name (get-value %))))

(defn- tentacle-source-input
  []
  (input "tentacle-source" (:source @form-data)
         #(swap! form-data assoc :source (get-value %))))

(defn- tentacle-command-input
  []
  [:textarea#tentacle-command
   {:on-change #(swap! form-data assoc :command (get-value %))}])

(defn tentacle-form
  [{:keys [csrf submit-url original-name heads] :or {original-name ""}}]
  (do
      (swap! form-data assoc :original-name original-name
                             :name original-name)
      (reset! csrf-token csrf)
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
         [button #(validate-form submit-url)]])))
