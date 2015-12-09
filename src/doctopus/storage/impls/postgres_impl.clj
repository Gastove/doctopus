(ns doctopus.storage.impls.postgres-impl
  (:require [doctopus.db :as db]
            [doctopus.files :as f])
  (:import [java.nio.file Files Paths]))

(def is-img-regex #"^image")

(defn get-mime-type
  [f]
  (let [path (.toPath f)]
    (Files/probeContentType path)))

(defn assign-body-and-image
  [base-doc doc-path mime-type]
  (let [doc-content (slurp doc-path)]
    (if (re-find is-img-regex mime-type)
      (assoc base-doc :body :image :image (.getBytes doc-content))
      (assoc base-doc :body doc-content :image nil))))

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
            :let [doc-mime-type (get-mime-type doc-file)
                  base-doc {:name (str tent-name "-" (.getName doc-file))
                            ;; :path doc-file
                            :mime-type doc-mime-type
                            :uri (str tent-name "/" relpath)
                            :tentacle-name tent-name}
                  new-doc (assign-body-and-image base-doc doc-file doc-mime-type)]]
      (db/save-document! new-doc false))
    (db/update-document-index)))

(defn load-fn
  [uri]
  (db/get-document-by-uri uri))

(defn remove-fn
  [document]
  (db/delete-document! document))
