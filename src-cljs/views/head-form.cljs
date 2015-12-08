(ns doctopus.views.head-form
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [clojure.string :as s]
            [doctopus.util :refer [get-value http-ok? redirect-to in?]]
            [doctopus.views.common :refer [button]]
            [reagent.core :refer [atom]])
            (:require-macros [cljs.core.async.macros :refer [go]]))

(defonce form-data (atom {}))
(defonce errors (atom [:name-empty]))
(defonce csrf-token (atom ""))
(defonce existing-heads (atom []))

(defn- name-taken?
  [heads head-name]
  (in? head-name (map :name heads)))

(defn- submit-form
  [submit-url]
  (let [data @form-data]
    (go
      (let [response (<! (http/post submit-url
                                    {:json-params data
                                     :headers {"X-CSRF-Token" @csrf-token}}))]
        (if (http-ok? (:status response))
          (redirect-to (:success-url response)))))))

(defn- name-invalid?
  [head-name]
  (and (not= head-name "") (->> head-name
      (re-matches #"[\w-_]+")
      nil?)))

(defn- name-empty?
  [head-name]
  (empty? (s/trim head-name)))

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

(defn- create-form-validator
  [validators lookup]
  (fn [key atom old new]
    (let [item (lookup new)]
      (reset! errors
              (filter #(not (nil? %))
                      (for [[f k] validators]
                        (if (f item) k)))))))

(defn head-form
  [{:keys [csrf submit-url original-name heads] :or {original-name ""}}]
  (let [validation-map [[(partial name-taken? heads) :name-taken]
                        [name-invalid? :name-invalid]
                        [name-empty? :name-empty]]]
    (do
      (swap! form-data assoc :original-name original-name
             :name original-name)
      (reset! csrf-token csrf)
      (reset! existing-heads heads)
      (add-watch form-data
                 :validation
                 (create-form-validator validation-map #(get % :name)))
      (fn []
        [:form.main
         [:div
          [:fieldset
           [:div
            [:label {:for "head-name"} "Head Name"]
            [head-input @form-data @errors]]]
          (if (> (count @errors) 0)
            [button #(submit-form submit-url) "Save" {:disabled true}]
            [button #(submit-form submit-url) "Save"])]]))))