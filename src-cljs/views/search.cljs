(ns doctopus.search
  (:require [cljs.core.async :refer [>!]]
            [clojure.string :as s]
            [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields init-field value-of]]))

(enable-console-print!)

(defonce default-search-state {:tentacle-local true
                               :tentacle-name nil
                               :loading false
                               :terms ""
                               :results []})

(defn- label
  [label-text id & elements]
  [:label {:for id} label-text (if-not (nil? elements) elements)])

(defn- input [label-text type id & elements]
  [:div.input
   (label label-text id)
   [:input.form-control {:field type :id id} (if-not (nil? elements) elements)]])

(defn- checkbox [label-text id]
  [:div.input.checkbox
   (label label-text id [:input.form-control {:field :checkbox :id id}])])

(defn- results-component
  [{:keys [results]}]
  [:div.results (for [result results]
    [:div {:key (keyword (:title result))} (:title result)])])

(defn- loading-component
  []
  [:div.loading "Loading…"])

(defn- form-template
  [search-state search-fn]
  (let [name (:tentacle-name search-state)]
    [:form {:on-submit search-fn}
     (input "Search" :text :terms
            [:input.search-button.btn.medium.primary {:key :search-button
                                                        :type :submit
                                                        :value "Search"}])
     (when name
       (checkbox "Search this project only" :tentacle-local))]))

(defn- create-search-fn
  [state]
  (fn [event]
    (.preventDefault event)
    (.stopPropagation event)
    (if-let [terms (not-empty (s/trim (:terms @state)))]
      (do
        (swap! state assoc :loading true)))))

(defn search-form
  "instantiates a new search form, optionally with an initial state"
  ([ctx] (search-form ctx default-search-state))
  ([ctx search-state]
   (let [tentacle-name (:tentacle-name ctx)
         tentacle-state-local (get search-state :tentacle-local (:tentacle-local default-search-state))
         tentacle-local (if (nil? tentacle-name)
                          false
                          tentacle-state-local)
         state (r/atom (assoc search-state :tentacle-name tentacle-name
                                           :tentacle-local tentacle-local))
         search-fn (create-search-fn state)]
     (fn []
       [:div.search-form
        [bind-fields (form-template @state search-fn) state]
        (when (:loading @state) [loading-component])
        (when (not-empty (:results @state))
          [results-component @state])]))))