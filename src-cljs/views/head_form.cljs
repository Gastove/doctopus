(ns doctopus.views.head-form
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [clojure.string :as s]
            [doctopus.util :refer [get-value http-ok? redirect-to in? maybe-conj]]
            [doctopus.validation :as validation]
            [doctopus.views.common :refer [button]]
            [reagent.core :refer [atom]])
            (:require-macros [cljs.core.async.macros :refer [go]]))

(defonce form-data (atom {}))
(defonce errors (atom [:name-empty]))
(defonce csrf-token (atom ""))
(defonce existing-heads (atom []))

(defn- submit-form
  [submit-url]
  (let [data @form-data]
    (go
      (let [response (<! (http/post submit-url
                                    {:json-params data
                                     :headers {"X-CSRF-Token" @csrf-token}}))]
        (if (http-ok? (:status response))
          (redirect-to (get-in response [:body :success-url])))))))

(defn- head-input
  [data errors]
  (let [head-name (:name data)]
    [:div
     [:input#head-name
      {:type "text"
       :value head-name
       :on-change #(swap! form-data assoc :name (get-value %))}]
     (when (in? :name-invalid errors)
       [:p "Head name invalid"])
     (when (in? :name-taken errors)
       [:p "Head name taken"])]))

(defn head-form
  [{:keys [csrf submit-url original-name heads] :or {original-name ""}}]
  (let [validation-map {:name [[(partial validation/field-duplicate-value? :name heads) :name-taken]
                               [validation/field-invalid-characters? :name-invalid]
                               [validation/field-empty? :name-empty]]}]
    (do
      (swap! form-data assoc :original-name original-name
             :name original-name)
      (reset! csrf-token csrf)
      (reset! existing-heads heads)
      (add-watch form-data
                 :validation
                 (validation/create-form-validator validation-map #(reset! errors %)))
      (fn []
        [:form.main
         [:div
          [:fieldset
           [:div
            [:label {:for "head-name"} "Head Name"]
            [head-input @form-data @errors]]]
          (maybe-conj (> (count @errors) 0) [button #(submit-form submit-url) "Save"] {:disabled "disabled"})]]))))
