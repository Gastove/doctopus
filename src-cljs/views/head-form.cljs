(ns doctopus.views.head-form
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [doctopus.util :refer [get-value http-ok? redirect-to not-in?]]
            [doctopus.views.common :refer [button]]
            [reagent.core :as reagent :refer [atom]])
            (:require-macros [cljs.core.async.macros :refer [go]]))

(def form-data (atom {}))
(def csrf-token (atom ""))
(def existing-heads (atom []))

(defn- name-unique?
  [name heads]
  (not-in? name (map :name heads)))

(defn- show-form-error
  [error]
  (println (str error)))

(defn- submit-form
  [submit-url]
  (let [data @form-data]
    (go
      (let [response (<! (http/post submit-url
                                    {:json-params data
                                     :headers {"X-CSRF-Token" @csrf-token}}))]
        (if (http-ok? (:status response))
          (redirect-to (:success-url response))
          (show-form-error (:error response)))))))

(defn- head-input-element
  ([] (head-input-element "input"))
  ([class-name]
    [(keyword (str "input#head-name." class-name))
     {:type "text"
      :value (:name @form-data)
      :on-change #(swap! form-data assoc :name (get-value %))}]))

(defn- head-input
  [data]
  (if (name-unique? (:name data) @existing-heads)
    [:div [head-input-element]]
    [:div [head-input-element "error"] [:p "Head name already exists"]]))

(defn head-form
  [{:keys [csrf submit-url original-name heads] :or {original-name ""}}]
    (do
      (swap! form-data assoc :original-name original-name
                             :name original-name)
      (reset! csrf-token csrf)
      (reset! existing-heads heads)
      (fn []
        [:form.main
          [:div
            [:fieldset
              [:div
                [:label {:for "head-name"} "Head Name"]
                [head-input @form-data]]]
            [button #(submit-form submit-url)]]])))
