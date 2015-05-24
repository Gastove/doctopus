(ns doctopus.main
  (:require [doctopus.views.head-form :as head-form]
            [goog.dom :as dom]
            [reagent.core :as reagent]))

(enable-console-print!)

(defn- get-app-state
  []
  (-> (dom/getElement "app-state")
      (.-textContent)
      (JSON/parse)
      (js->clj :keywordize-keys true)))

(defn init
  []
  (let [app-state (get-app-state)]
    (println app-state)))

(init)
