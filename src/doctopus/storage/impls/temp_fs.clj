(ns doctopus.storage.impls.temp-fs
  (:require [clojure.string :as str]
            [doctopus.files :as files]
            [doctopus.storage.impls.fs-impl :refer [save-html-file] :as fs-impl]
            [me.raynes.fs :as fs]
            [bidi.ring :as bidi-ring]))

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
  [key]
  (let [file-handle (binding [fs/*cwd* @temp-dir] (fs/file key))
        file-name (str (.getPath file-handle) "/")]
    (if (fs/exists? file-handle)
      [key {"/" (bidi-ring/->Files {:dir file-name})}]
      nil)))

(defn remove-fn
  [key]
  (fs-impl/remove-html @temp-dir key))
