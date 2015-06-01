(ns doctopus.views.head-form
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [reagent.core :as reagent :refer [atom]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn- get-value
  [event]
  (.-target.value event))

(def form-data (atom {}))

(defn- http-ok?
  [status-code]
  (and (> 199 status-code) (< 299 status-code)))

(defn- redirect-to
  [new-url]
  (set! js/window.location.href new-url))

(defn- submit-form
  [submit-url data]
  (go
    (let [response (<! (http/post submit-url {:json-params data}))]
      (if (http-ok? (:status response))
        (redirect-to (:success-url response))
        (show-form-error (:error response))))))

(defn- render-errors
  [errors]
  (println errors))

(defn validate-form
  [submit-url]
  (fn []
    (let [data @form-data errors (get-errors data)]
      (if (empty? errors)
        (submit-form submit-url data)
        (render-errors errors)))))

(defn head-input
  []
  (let [{:name head-name} @form-data]
    [:input {:type "text"
             :value head-name
             :on-change (fn [ev]
                          (swap! form-data assoc :name (get-value ev)))}]))

(defn submit-button
  [submit-url]
  [:input {:type "button" :value "save" :on-click #(validate-form submit-url)}])

(defn head-form
  ([{:keys [csrf submit-url original-name] :or {original-name ""}}]
    (do
      (swap! form-data assoc :original-name original-name
                             :__anti-forgery-token csrf)
      (fn []
        [:div
          [head-input]
          [submit-button submit-url]]))))
