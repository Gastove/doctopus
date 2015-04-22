(ns doctopus.storage.storage-impls.permanent-fs
  (:require [doctopus
             [configuration :refer [server-config]]
             [files :as files]]
            [me.raynes.fs :as fs]
            [doctopus.storage.storage-impls.fs-impl :refer [save-html-file]]))

(def root (fs/file (:permanent-fs-root (server-config))))
(if-not (fs/exists? root) (fs/mkdirs root))

(defn save-fn
  [key path-html-pairs]
  (doseq [[path html] path-html-pairs] (save-html-file root key path html)))

(defn load-fn
  "This is as Version 1 a way of doing this as I can think of:

  1. We load html
  2. We wrap that html in a function that returns itself, so we can Bidi"
  [key]
  (binding [fs/*cwd* root]
    (let [dir (fs/file key)
          rel-path-html-pairs (files/read-html dir)]
      (into [] (map (fn [[rel-path html]] [rel-path (fn [_] html)]) rel-path-html-pairs)))))
