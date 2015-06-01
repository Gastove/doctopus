(ns doctopus.main
  (:require [goog.dom :as dom]
            [doctopus.views.head-form :refer [head-form]]
            [reagent.core :as reagent]))

;(enable-console-print!)

(def pages {:add-head head-form})

(defn- get-app-state
  []
  (-> (dom/getElement "app-state")
      (.-textContent)
      (js/JSON.parse)
      (js->clj :keywordize-keys true)))

(defn- get-page-component
  [app-state]
  (((keyword (:page app-state)) pages)) app-state)

(defn init
  [app-state]
    (reagent/render-component [head-form] (dom/getElement "app-content")))

(init (get-app-state))
