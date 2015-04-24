(ns doctopus.storage.impls.permanent-fs
  (:require [bidi.ring :as bidi-ring]
            [doctopus
             [configuration :refer [server-config]]
             [files :as files]]
            [doctopus.storage.impls.fs-impl :refer [save-html-file] :as fs-impl]
            [me.raynes.fs :as fs]))

;; TODO: this does not actually work the way we need it to! Neat! --RMD

(def root (fs/file (:permanent-fs-root (server-config))))
(if-not (fs/exists? root) (fs/mkdirs root))

(defn save-fn
  "Save a dir of html stuff by moving it in to the annointed dir in
  the permanent fs"
  [key src-dir]
  (binding [fs/*cwd* root]
    (let [target-dir (fs/file key)
          result (fs/copy-dir src-dir target-dir)]
      (and (fs/exists? result)
           (fs/readable? result)))))

(defn load-fn
  "Returns the routes this serves"
  [key]
  (let [file-handle (binding [fs/*cwd* root] (fs/file key))
        file-name (str (.getPath file-handle) "/")]
    [key {"/" (bidi-ring/->Files {:dir file-name})}]))

(defn remove-fn
  [key]
  (fs-impl/remove-html root key))
