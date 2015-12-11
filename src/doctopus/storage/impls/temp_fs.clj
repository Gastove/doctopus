(ns doctopus.storage.impls.temp-fs
  (:require [clojure.string :as str]
            [doctopus.configuration :refer [docs-uri-prefix]]
            [doctopus.files :as files]
            [doctopus.storage.impls.fs-impl :refer [save-html-file] :as fs-impl]
            [me.raynes.fs :as fs]
            [ring.util.response :as response]
            [ring.util.mime-type :refer [ext-mime-type]]
            [taoensso.timbre :as log]))

;; The root of the temp filesystem. Each Thing will store its stuff
;; within this directory
(def temp-dir (atom (fs/temp-dir  "doctopus-temp")))

(defn count-fn [tentacle]
  (binding [fs/*cwd* (fs/file @temp-dir docs-uri-prefix)]
    (let [target-dir (fs/file (:name tentacle))]
      (count (files/walk-docs-dir target-dir)))))

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

(defn- build-response
  [file-handle]
  (let [body (slurp file-handle)
        file-name (.getPath file-handle)
        mime-type (ext-mime-type file-name)]
    (-> (response/response body)
        (response/content-type mime-type))))

(defn load-fn
  "Returns the routes this serves"
  [uri]
  (let [rel-path (str/replace uri (str docs-uri-prefix "/") "") ;; Remove URI prefix to get relative path
        file-handle (binding [fs/*cwd* @temp-dir] (fs/file rel-path))]
    (if (fs/exists? file-handle)
      (build-response file-handle)
      nil)))

(defn remove-fn
  [key]
  (log/debug "Removing" key "from" @temp-dir)
  (fs-impl/remove-html @temp-dir key))
