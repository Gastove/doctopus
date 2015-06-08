(ns doctopus.views.head-form
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [doctopus.util :as util :refer [get-value http-ok? redirect-to]]
            [reagent.core :as reagent :refer [atom]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def form-data (atom {}))

(defn- show-form-error
  [error]
  (println (str error)))

(defn- submit-form
  [submit-url data]
  (go
    (let [response (<! (http/post submit-url
                                  {:json-params data
                                   :headers {"X-CSRF-Token" (:csrf data)}}))]
      (if (http-ok? (:status response))
        (redirect-to (:success-url response))
        (show-form-error (:error response))))))

(defn- render-errors
  [errors]
  (println errors))

(defn- get-errors
  [form-data]
  [])

(defn validate-form
  [submit-url]
  (let [data @form-data errors (get-errors data)]
    (if (empty? errors)
      (submit-form submit-url data)
      (render-errors errors))))

(defn head-input
  []
  [:input#head-name
   {:type "text"
    :value (:name @form-data)
    :on-change (fn [ev]
                 (swap! form-data assoc :name (get-value ev)))}])

(defn submit-button
  [submit-url]
  [:input.btn.medium.secondary {:type "button"
           :value "Save"
           :on-click #(validate-form submit-url)}])

(defn head-form
  [{:keys [csrf submit-url original-name] :or {original-name ""}}]
    (do
      (swap! form-data assoc :original-name original-name
                             :name original-name
                             :csrf csrf)
      (fn []
        [:form.main
          [:div
            [:fieldset
              [:div
                [:label {:for "head-name"} "Head Name"]
                [head-input]]]
            [submit-button submit-url]]])))
