(ns doctopus.views.index)

(defn tentacle-items
  [tentacles]
  [:ul
    (for [tentacle tentacles]
      (let [tentacle-name (:name tentacle)
            tentacle-location (:location tentacle)]
        ^{:key tentacle-name} [:li
                               [:a {:href tentacle-location} tentacle-name]]))])

(defn head-items
  [heads]
  [:ul
    (for [head heads]
      (let [head-name (:name head) head-location (:location head)]
        ^{:key head-name} [:li [:a {:href head-location} head-name]]))])

(defn main
  [{:keys [heads tentacles]}]
  (fn []
    [:main
     [:section.module
      [:div.sub-header
       [:h2 "Heads"]
       [:div.sub-header-actions
        [:a.btn.medium.primary {:href "/add-head"} "Add a head"]]]
      [head-items heads]]
     [:section.module
      [:div.sub-header
       [:h2 "Tentacles"]
       [:div.sub-header-actions
        [:a.btn.medium.primary {:href "/add-tentacle"} "Add a tentacle"]]]
      [tentacle-items tentacles]]]))
