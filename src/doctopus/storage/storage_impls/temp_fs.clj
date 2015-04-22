(ns doctopus.files.storage-impls.temp-fs
  (:require [me.raynes.fs :as fs]
            [doctopus.files.storage-impls :refer :all])
  (:import [doctopus.files.storage-impls.BackendImplementation]))

;; The root of the temp filesystem. Each Thing will store its stuff
;; within this directory
(def temp-dir (atom (fs/temp-dir "doctopus-temp")))

(defn regenerate-temp-dir
  "Generates a new temp dir"
  []
  (swap! temp-dir fs/temp-dir "doctopus-temp"))

(defn- load-html-file
  [rel-path]
  (let [html-re #"\.html$"
        assure-suffix #(if (re-find html-re rel-path) rel-path (str rel-path ".html"))
        file-handle (fs/file @*temp-dir* rel-path)]
    (slurp file-handle)))

(defn save-html-file
  "Expects a key, a relative path, and the data to write. `Key' is
  assumed to be the name of a particular documentation set, and
  becomes the primary subdirectory in the filesystem."
  [key rel-path data]
  (binding [fs/*cwd* @temp-dir]
    (let [file-handle (fs/file key rel-path)]
      (spit file-handle data))))

(defn save-fn
  [key path-html-pairs]
  (doseq [[path html] path-html-pairs] (save-html-file key path html)))

(defn load-fn
  [key])
