(ns doctopus.test-utilities
  (:require [me.raynes.fs :as fs]))

(defn make-temp-root
  ([]
   "Make a default temp-directory."
   (make-temp-root "doctopus-test"))
  ([dir-path]
  "Make a fake directory root to use with doctopus test functions."
  (.getPath (fs/temp-dir dir-path))))

(defn make-temp-root []
  "Default parameterized version of make-temp-root."
  (make-temp-root "doctopus-test"))

(defn make-temp-dir-tree [tmp-root]
  "Make a directory tree in the temporary root.

  The idea is that our tmp-root will also be our documentation root.
  All the sub directories we create will be searched like how a repository
  is searched
  "
  (binding [fs/*cwd* tmp-root]
    (let [dir-names ["docs" "references" "manual"]
          file-handles (into []
                             (for [dir-name dir-names]
                               (fs/file dir-name)))]

          file-handles)))
