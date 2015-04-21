(ns doctopus.configuration
  (:require [nomad :refer [defconfig]]
            [clojure.java.io :as io]))

(defconfig server-config (io/resource "configuration.edn"))
