(ns doctopus.files.storage-impls.temp-fs
  (:require [me.raynes.fs :as fs]))

(def *temp-dir* (fs/temp-dir))
