(ns doctopus.omni
  (:require [goog.dom :as dom]
            [reagent.core :as r]
            [doctopus.util :refer [get-app-state]]
            [doctopus.search :refer [search-form]]))

(enable-console-print!)

(defonce state (r/atom {:open false}))

(defn- open-close-omnibar
  [event]
  (.stopPropagation event)
  (.preventDefault event)
  (swap! state assoc :open (not (:open @state))))

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

(defn- state-component
  [app-state]
  (let [{:keys [tentacle-name]} app-state]
    [:div.state
     [:h1
      [:small "currently viewing "]
      [:a {:href (str "/docs/" tentacle-name)} tentacle-name]]]))

(defn- sidebar-component
  [full-state]
  (let [context (:context full-state)]
    [:div#sidebar
     [logo-component]
     [close-component]
     [state-component context]
     [search-form context]]))


(defn omnibar
  [context]
  (swap! state assoc :context context)
  (let [{:keys [open]} @state]
    (if open
      [sidebar-component @state]
      [brand-component])))

(defn init
  [component]
  (r/render-component component (dom/getElement "doctopus-omnibar")))

(init [omnibar (get-app-state)])
