(ns doctopus.views.admin
  (:require [cljs.core.async :refer [<! chan]]
            [reagent.core :as r]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [doctopus.views.head :refer [create-head-form create-head-list]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(enable-console-print!)

(declare route-home route-head route-tentacle)

(defonce app-state (r/atom {}))
(defonce heads (r/atom {}))

(defn- flip
  [coll k]
  (assoc coll k (not (k coll))))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "What would you like to do today?"]
   [:ul
    [:li [:a {:href (route-head)} "Deal with Heads"]]
    [:li [:a {:href (route-tentacle)} "Deal with Tentacles"]]]])

(defn head-page []
  (let [page-state (r/atom {:edit false})
        head-channel (chan)
        edit-channel (chan)
        head-form (create-head-form head-channel)
        head-list (create-head-list edit-channel)]
    (fn []
      (go-loop []
        (let [response (<! head-channel)
              head (:body response)
              head-name (keyword (:name head))]
          (swap! heads assoc head-name head)
          (swap! page-state flip :edit)
          (recur)))
      [:div.row
       [:div.col.six.offset-3
        (when (:edit @page-state)
          [:div [head-form {:submit-url "/jsapi/v1/heads"
                            :csrf       (:csrf @app-state)
                            :heads      (map last @heads)}]
           [:button.btn.secondary {:on-click #(swap! page-state flip :edit)} "Cancel"]])
        [:section.module
         [:div.sub-header
          [:h2 "Heads"]
          (when (not (:edit @page-state))
            [:div.sub-header-actions
             [:button.btn.primary {:on-click #(swap! page-state flip :edit)} "Add New"]])]
         [head-list {:heads (map last @heads)}]]]])))

(defn tentacle-page []
  [:div [:h2 "Tentacle!"]
   [:div [:a {:href (route-home)} "go home"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute route-home "/admin"
  []
  (session/put! :current-page #'home-page))

(secretary/defroute route-head "/admin/head"
  []
  (session/put! :current-page #'head-page))

(secretary/defroute route-tentacle "/admin/tentacle"
  []
  (session/put! :current-page #'tentacle-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [current-page] (.getElementById js/document "app-content")))

(defn init! [initial-state]
  (let [h (get initial-state :heads [])
        heads-map (zipmap (map #(keyword (:name %)) h) h)]
    (reset! app-state initial-state)
    (reset! heads heads-map)
    (accountant/configure-navigation!)
    (accountant/dispatch-current!)
    (mount-root)))
