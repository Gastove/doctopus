(ns doctopus.dev.main
  (:require [doctopus.main :as main]
            [doctopus.omni :as omni]
            [doctopus.util :refer [get-app-state]]))

(def app-state (get-app-state))

(main/init! app-state)
(omni/init! [omni/omnibar app-state])
