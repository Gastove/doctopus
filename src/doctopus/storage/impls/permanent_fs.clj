(ns doctopus.storage.impls.permanent-fs
  (:require [doctopus
             [configuration :refer [server-config]]
             [files :as files]]
            [me.raynes.fs :as fs]
            [doctopus.storage.impls.fs-impl :refer [save-html-file] :as fs-impl]))

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
  "This is as Version 1 a way of doing this as I can think of:

  1. We load html
  2. We wrap that html in a function that returns itself, so we can Bidi"
  [key]
  (binding [fs/*cwd* root]
    (let [dir (fs/file key)
          rel-path-html-pairs (files/read-html dir)]
      (into [] (map (fn [[rel-path html]] [rel-path (fn [_] html)]) rel-path-html-pairs)))))

(defn remove-fn
  [key]
  (fs-impl/remove-html root key))
