(ns doctopus.storage.storage-impls.permanent-fs
  (:require [clojure.string :as str]
            [doctopus.files :as files]
            [doctopus.files.storage-impls :refer :all]
            [me.raynes.fs :as fs]
            [doctopus.configuration :refer [server-config]])
  (:import [doctopus.files.storage-impls.BackendImplementation]))

(def root (fs/file (:permanent-fs-root (server-config))))
(if-not (fs/exists? root) (fs/mkdirs root))

(defn- load-html-file
  [rel-path]
  (let [html-re #"\.html$"
        assure-suffix #(if (re-find html-re rel-path) rel-path (str rel-path ".html"))
        file-handle (fs/file root rel-path)]
    (slurp file-handle)))

(defn save-html-file
  "Expects a key, a relative path, and the data to write. `Key' is
  assumed to be the name of a particular documentation set, and
  becomes the primary subdirectory in the filesystem."
  [key rel-path data]
  (binding [fs/*cwd* root]
    (let [assure-relativity #(if (re-find #"^/" %) (subs % 1) %)
          file-handle (fs/file key (assure-relativity rel-path))]
      (fs/mkdirs (fs/parent file-handle))
      (spit file-handle data))))

(defn save-fn
  [key path-html-pairs]
  (doseq [[path html] path-html-pairs] (save-html-file key path html)))

(defn load-fn
  "This is as Version 1 a way of doing this as I can think of:

  1. We load html
  2. We wrap that html in a function that returns itself, so we can Bidi"
  [key]
  (binding [fs/*cwd* root]
    (let [dir (fs/file key)
          rel-path-html-pairs (files/read-html dir)]
      (into [] (map (fn [[rel-path html]] [rel-path (fn [_] html)]) rel-path-html-pairs)))))
