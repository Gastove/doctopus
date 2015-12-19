(ns doctopus.main
  (:require [goog.dom :as dom]
            [doctopus.views.tentacle-form :refer [tentacle-form]]
            [doctopus.views.index :refer [main]]
            [doctopus.views.admin :as admin]
            [doctopus.util :refer [get-app-state]]
            [reagent.core :as reagent]))

(def pages {:add-tentacle tentacle-form
            :edit-tentacle tentacle-form
            :index main})

(defn- get-page-component
  [app-state]
  (if-let [page (:page app-state)]
    [((keyword page) pages) app-state]))

(defn init!
  [app-state]
  (if (= (:page app-state) "admin")
    (admin/init! app-state)
    (if-let [page-component (get-page-component app-state)]
      (reagent/render-component page-component (dom/getElement "app-content"))
      (js/console.debug "Abandoning init: no component is defined for this page"))))
