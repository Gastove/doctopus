(ns doctopus.views.head-form
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [doctopus.util :refer [get-value http-ok? redirect-to in?]]
            [doctopus.views.common :refer [button]]
            [reagent.core :as reagent :refer [atom]])
            (:require-macros [cljs.core.async.macros :refer [go]]))

(def form-data (atom {}))
(def csrf-token (atom ""))
(def existing-heads (atom []))
(def errors (atom []))

(defn- name-taken?
  [head-name heads]
  (in? head-name (map :name heads)))

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

(defn- name-invalid?
  [head-name]
  (and (not= head-name "") (->> head-name
      (re-matches #"[\w-_]+")
      nil?)))

(defn- head-input
  [data]
  (let [head-name (:name data)]
    [:div
      [:input#head-name
       {:type "text"
        :value head-name
        :on-change #(swap! form-data assoc :name (get-value %))}]
      (when (name-invalid? head-name)
                           [:p "Head name invalid"])
      (when (name-taken? head-name @existing-heads)
                           [:p "Head name taken"])]))

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
