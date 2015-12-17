(ns doctopus.views.head-form
  (:require [cljs.core.async :refer [<! >! chan]]
            [cljs-http.client :as http]
            [doctopus.util :refer [get-value http-ok? redirect-to in? maybe-conj]]
            [doctopus.validation :as validation]
            [doctopus.views.common :refer [button create-input create-submit-form]]
            [reagent.core :refer [atom]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def head-name-errors {:name-invalid "Head name invalid"
                       :name-taken   "Head name taken"})

(defn create-head-form
  [channel]
  (fn
    [{:keys [csrf submit-url original-name heads] :or {original-name ""}}]
    (let [validation-map {:name [[(partial validation/field-duplicate-value? :name heads) :name-taken]
                                 [validation/field-invalid-characters? :name-invalid]
                                 [validation/field-empty? :name-empty]]}
          validator (validation/create-form-validator validation-map)
          form-atom (atom {:original-name original-name :name original-name})
          errors (atom [:name-empty])
          head-input (create-input :name form-atom head-name-errors)
          submit-form! (create-submit-form submit-url csrf)
          on-submit (fn [event]
                      (.preventDefault event)
                      (.stopPropagation event)
                      (go (>! channel (<! (submit-form! @form-atom)))))]
      (go-loop []
        (reset! errors (<! validator))
        (recur))
      (do
        (add-watch form-atom :validation #(go (>! validator %4)))
        (fn []
          [:form.main {:on-submit on-submit}
           [:div
            [:fieldset
             [:div
              [:label {:for "head-name"} "Head Name"]
              [head-input @form-atom @errors]]]
            [:input
             (maybe-conj (> (count @errors) 0) {:type :submit :value "Save"} {:disabled true})]]])))))
