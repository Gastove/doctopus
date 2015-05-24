(ns doctopus.views.head-form
  (:require [cljs.core.async :refer [<!]]
            [reagent.core :as reagent :refer [atom]])
  (:require-macros [clsj.core.async.macros :refer [go]]))

(defn- get-value
  [event]
  (.-target.value event))

(def form-data (atom {}))

(defn- http-ok?
  [status-code]
  (and (> 199 status-code) (< 299 status-code)))

(defn- redirect-to
  [new-url]
  (set! (.-href (.-location js/window) new-url)))

(defn- submit-form
  [submit-url data]
  (go
    (let [response (<! (http/post submit-url))]
      (if (http-ok? (:status response))
        (redirect-to (:success-url response))
        (show-form-error (:error response))))))

(defn validate-form
  [submit-url]
  (fn []
    (let [data @form-data
          errors (get-errors data)]
      (if (empty? errors)
        (submit-form submit-url data)
        (render-errors errors)))))

(defn head-input
  []
  (let [{:name head-name} @form-data]
    [:input {:type "text"
             :value head-name
             :on-change (fn [ev] swap! form-data assoc :name (get-value ev))}]))

(defn submit-button
  [submit-url]
  [:input {:type "button" :value "save" :on-click (validate-form submit-url)}])

(defn head-form
  ([submit-url] (head-form submit-url ""))
  ([submit-url original-name]
    (do
      (swap! form-data assoc :original-name original-name)
      (fn []
        [:div
          [head-input]
          [submit-button submit-url]]))))
