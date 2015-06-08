(ns doctopus.views.head-form
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [doctopus.util :refer [get-value http-ok? redirect-to]]
            [doctopus.view.common :refer [button]]
            [reagent.core :as reagent :refer [atom]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def form-data (atom {}))
(def csrf-token (atom ""))

(defn- show-form-error
  [error]
  (println (str error)))

(defn- submit-form
  [submit-url data]
  (go
    (let [response (<! (http/post submit-url
                                  {:json-params data
                                   :headers {"X-CSRF-Token" @csrf-token}}))]
      (if (http-ok? (:status response))
        (redirect-to (:success-url response))
        (show-form-error (:error response))))))

(defn- render-errors
  [errors]
  (println errors))

(defn- get-errors
  [form-data]
  [])

(defn- validate-form
  [submit-url]
  (let [data @form-data errors (get-errors data)]
    (if (empty? errors)
      (submit-form submit-url data)
      (render-errors errors))))

(defn- head-input
  []
  [:input#head-name
   {:type "text"
    :value (:name @form-data)
    :on-change #(swap! form-data assoc :name (get-value %))}])

(defn head-form
  [{:keys [csrf submit-url original-name] :or {original-name ""}}]
    (do
      (swap! form-data assoc :original-name original-name
                             :name original-name)
      (reset! csrf-token csrf)
      (fn []
        [:form.main
          [:div
            [:fieldset
              [:div
                [:label {:for "head-name"} "Head Name"]
                [head-input]]]
            [button #(validate-form submit-url)]]])))
