(ns doctopus.channels
  (:require [clojure.core.async :as async]))

(def build-channel (async/chan 100))
