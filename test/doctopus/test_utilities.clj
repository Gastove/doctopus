(ns doctopus.test-utilities
  (:require [me.raynes.fs :as fs]))

(defn make-temp-root
  "Database"
  ([]
   (make-temp-root "doctopus-test"))
  ([dir-path]
  (.getPath (fs/temp-dir dir-path))))

(defn make-temp-dir-tree
  "Make a directory tree in the temporary root.

  The idea is that our tmp-root will also be our documentation root.
  All the sub directories we create will be searched like how a repository
  is searched
  "
  [tmp-root]
  (binding [fs/*cwd* tmp-root]
    (let [dir-names ["docs" "references" "manual"]
          file-handles (into []
                             (for [dir-name dir-names]
                               (fs/file dir-name)))]

          file-handles)))
