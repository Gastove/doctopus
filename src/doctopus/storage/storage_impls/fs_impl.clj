(ns doctopus.storage.storage-impls.fs-impl
  (:require [doctopus.files.storage-impls :refer :all]
            [me.raynes.fs :as fs]
            [me.raynes.fs :as fs]))


(defn- load-html-file
  [root rel-path]
  (let [html-re #"\.html$"
        assure-suffix #(if (re-find html-re rel-path) rel-path (str rel-path ".html"))
        file-handle (fs/file root rel-path)]
    (slurp file-handle)))


(defn save-html-file
  "Expects a key, a relative path, and the data to write. `Key' is
  assumed to be the name of a particular documentation set, and
  becomes the primary subdirectory in the filesystem."
  [root key rel-path data]
  (binding [fs/*cwd* root]
    (let [assure-relativity #(if (re-find #"^/" %) (subs % 1) %)
          file-handle (fs/file key (assure-relativity rel-path))]
      (fs/mkdirs (fs/parent file-handle))
      (spit file-handle data))))

(defn remove
  "Given a root and a key, delete the entire doc dir"
  [root key]
  (binding [fs/*cwd* root]
    (let [dir (fs/file key)]
      (fs/delete-dir key))))
