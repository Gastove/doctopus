(ns doctopus.omni
  (:require [goog.dom :as dom]
            [reagent.core :as reagent]))

(defn omnibar
  []
  (fn []
    [:div#brand
     [:a {:href "/"} "doctopus"]]))

(defn init
  [component]
  (reagent/render-component component (dom/getElement "doctopus-omnibar")))

(init (omnibar))
