(ns doctopus.views.common)

(defn button
  ([on-click] (button on-click "Save"))
  ([on-click button-text] (button on-click button-text {}))
  ([on-click button-text opts]
   [:input.btn.medium.secondary (apply assoc opts [:type "button"
                                                   :value button-text
                                                   :on-click on-click])]))
