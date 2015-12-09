(ns doctopus.omni
  (:require [goog.dom :as dom]
            [reagent.core :as r]))

(enable-console-print!)

(defonce state (r/atom {:open false}))

(defn- open-close-omnibar
  [event]
  (.stopPropagation event)
  (.preventDefault event)
  (swap! state assoc :open (not (:open @state)))
  (println @state))

(defn- brand-component
  []
  [:div#brand
     [:a {:href "#"
          :on-click open-close-omnibar} "doctopus"]])

(defn- close-component
  []
  [:div.icon.close
   [:a {:href "#"
          :on-click open-close-omnibar} "close"]])

(defn- logo-component
  []
  [:div.logo [:a {:href "/"} "doctopus"]])

(defn- sidebar-component
  []
  [:div#sidebar
   [logo-component]
   [close-component]])


(defn omnibar
  []
  (let [{:keys [open]} @state]
    (if open
      [sidebar-component]
      [brand-component])))

(defn init
  [component]
  (r/render-component component (dom/getElement "doctopus-omnibar")))

(init [omnibar])
