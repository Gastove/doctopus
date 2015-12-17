(ns doctopus.views.common
  (:require [cljs.core.async :refer [<! >! chan]]
            [cljs-http.client :as http]
            [doctopus.util :refer [get-value]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn button
  ([on-click] (button on-click "Save"))
  ([on-click button-text] (button on-click button-text {}))
  ([on-click button-text opts]
   [:input.btn.medium.secondary (apply assoc opts [:type "button"
                                                   :value button-text
                                                   :on-click on-click])]))

(defn create-submit-form
  [submit-url csrf-token]
  (fn [form-data]
    (go
      (<! (http/post submit-url
                     {:json-params form-data
                      :headers {"X-CSRF-Token" csrf-token}})))))

(defn create-input
  [id form-atom error-list]
  (fn [form-data errors]
    (let [value (get form-data id)]
      [:div
       [:input
        {:id (name id)
         :type "text"
         :value value
         :on-change #(swap! form-atom assoc id (get-value %))}]
       (for [err (filter #(not (nil? %)) (map #(get error-list %) errors))]
         [:p err])])))
