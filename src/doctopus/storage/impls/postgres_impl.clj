(ns doctopus.storage.impls.postgres-impl
  (:require [doctopus.db :as db]
            [doctopus.files :as f])
  (:import [java.nio.file Files Paths]))

(def is-img-regex
  #"\.(png|pdf|jpg|jpeg|gif|ico|){1}$")

(defn get-mime-type
  [path-str]
  (let [path (Paths/get path-str (into-array String []))]
    (Files/probeContentType path)))

(defn save-fn
  "Load every document from a directory in to the database.

  Note: at time of writing, at the end of load the call to update-document-index
  will recalculate the _entire_ `search_vector' column in PG. If this ever gets
  too costly, it can be improved, probably by adding additional info to the
  update's `where' clause.

  Also note: there are two kinds of files to be loaded: text and images (binary)."
  [tentacle src-dir]
  (let [relpath-file-pairs (f/list-files-with-relative-paths src-dir)
        tent-name (:name tentacle)]
    (doseq [[relpath doc-file] relpath-file-pairs
            :let [new-doc {:name (str tent-name "-" (.getName doc-file))
                           :body (slurp doc-file)
                           :path doc-file
                           :type (get-mime-type doc-file)
                           :uri (str tent-name "/" relpath)
                           :tentacle-name tent-name}]]
      (db/save-document! new-doc))
    (db/update-document-index)))

(defn load-fn
  [uri]
  (db/get-document-by-uri uri))

(defn remove-fn
  [document]
  (db/delete-document! document))
