(ns doctopus.main
  (:require [goog.dom :as dom]
            [doctopus.views.head-form :refer [head-form]]
            [doctopus.views.tentacle-form :refer [tentacle-form]]
            [doctopus.views.index :refer [main]]
            [reagent.core :as reagent]))

(enable-console-print!)

(def pages {:add-head head-form
            :add-tentacle tentacle-form
            :edit-tentacle tentacle-form
            :index main})

(defn- get-app-state
  []
  (-> (dom/getElement "app-state")
      (.-textContent)
      (js/JSON.parse)
      (js->clj :keywordize-keys true)))

(defn- get-page-component
  [app-state]
  (if-let [page (:page app-state)]
    [((keyword page) pages) app-state]))

(defn init
  [app-state]
  (if-let [page-component (get-page-component app-state)]
    (reagent/render-component page-component (dom/getElement "app-content"))
    (js/console.debug "Abandoning init: no component is defined for this page")))

(init (get-app-state))
