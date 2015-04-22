(ns doctopus.files.storage-impls.temp-fs
  (:require [me.raynes.fs :as fs]
            [doctopus.files.storage-impls :refer :all])
  (:import [doctopus.files.storage-impls.BackendImplementation]))

;; The root of the temp filesystem. Each Thing will store its stuff
;; within this directory
(def ^:dynamic *temp-dir* (atom (fs/temp-dir "doctopus-temp")))

(defn regenerate-temp-dir
  "Generates a new temp dir"
  []
  (swap! *temp-dir* fs/temp-dir "doctopus-temp"))

(defn- load-html-file
  [rel-path]
  (let [html-re #"\.html$"
        assure-suffix #(if (re-find html-re rel-path) rel-path (str rel-path ".html"))
        file-handle (fs/file @*temp-dir* rel-path)]
    (slurp file-handle)))

(defn save-fn
  [])
;; Reminder of how to do this
;; (binding [fs/*cwd* @*temp-dir*]
;;   )
