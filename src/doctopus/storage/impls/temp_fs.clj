(ns doctopus.storage.impls.temp-fs
  (:require [clojure.string :as str]
            [doctopus.files :as files]
            [doctopus.storage.impls.fs-impl :refer [save-html-file] :as fs-impl]
            [me.raynes.fs :as fs]
            [taoensso.timbre :as log]))

;; The root of the temp filesystem. Each Thing will store its stuff
;; within this directory
(def temp-dir (atom (fs/temp-dir  "doctopus-temp")))

(defn regenerate-temp-dir
  "Generates a new temp dir"
  []
  (swap! temp-dir fs/temp-dir "doctopus-temp"))

(defn save-fn
  "Save a dir of html stuff by moving it in to the current temp root"
  [key src-dir]
  (binding [fs/*cwd* @temp-dir]
    (let [target-dir (fs/file key)
          result (fs/copy-dir src-dir target-dir)]
      (and (fs/exists? result)
           (fs/readable? result)))))

(defn load-fn
  "Returns the routes this serves"
  [uri]
  (let [rel-path (str/replace uri (str docs-uri-prefix "/") "") ;; Remove URI prefix to get relative path
        file-handle (binding [fs/*cwd* @temp-dir] (fs/file rel-path))
        file-name (.getPath file-handle)]
    (if (fs/exists? file-handle)
      (file-response file-name)
      nil)))

(defn remove-fn
  [key]
  (log/debug "Removing" key "from" @temp-dir)
  (fs-impl/remove-html @temp-dir key))
