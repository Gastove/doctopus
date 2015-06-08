(ns doctopus.views.common)

(defn button
  ([on-click] (button on-click "Save"))
  ([on-click button-text]
  [:input.btn.medium.secondary {:type "button"
                                :value button-text
                                :on-click on-click}]))
