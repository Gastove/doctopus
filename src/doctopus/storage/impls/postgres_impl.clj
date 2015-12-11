(ns doctopus.storage.impls.postgres-impl
  (:require [doctopus.configuration :as configuration]
            [doctopus.db :as db]
            [doctopus.files :as f]
            [ring.util.mime-type :as ring-mime]
            [ring.util.response :as response]
            [taoensso.timbre :as log])
  (:import [java.io ByteArrayInputStream]
           [java.nio.file Files Paths]))

(def is-img-regex #"^image")

(defn count-fn [tentacle]
  (count (db/get-all-documents-for-tentacle tentacle)))

(defn get-mime-type
  [f]
  (let [path (.toPath f)]
    (Files/probeContentType path)))

(defn assign-body-and-image
  [base-doc doc-file mime-type]
  (log/debug base-doc)
  (let [doc-content (slurp doc-file)]
    (if (re-find is-img-regex mime-type)
      (assoc base-doc :body "image" :image (.getBytes doc-content))
      (assoc base-doc :body doc-content :image nil))))

(defn save-fn
  "Load every document from a directory in to the database.

  Note: at time of writing, at the end of load the call to update-document-index
  will recalculate the _entire_ `search_vector' column in PG. If this ever gets
  too costly, it can be improved, probably by adding additional info to the
  update's `where' clause.

  Also note: there are two kinds of files to be loaded: text and images (binary)."
  [tent-name src-dir]
  (let [relpath-file-pairs (f/list-files-with-relative-paths src-dir)]
    (doseq [[relpath doc-file] relpath-file-pairs
            :let [doc-mime-type (ring-mime/ext-mime-type relpath)
                  base-doc {:name (str tent-name "-" (.getName doc-file))
                            ;; :path doc-file
                            :mime-type doc-mime-type
                            :uri (str configuration/docs-uri-prefix "/" tent-name relpath)
                            :tentacle-name tent-name}
                  new-doc (assign-body-and-image base-doc doc-file doc-mime-type)]]
      (log/debug "Reading doc from:" relpath)
      (log/debug "Giving doc URI:" (:uri new-doc))
      (db/save-document! new-doc false))
    (db/update-document-index)))

(defn- build-body
  "Examine a DB result to see if it's an image; if so, build a ByteArrayInputStream
  and use it as the response body."
  [res]
  (if (nil? (:image res))
    res
    (let [bais (ByteArrayInputStream. (:image res))]
      (assoc res :body bais))))

(defn- make-response
  [res]
  (log/debug "Content type from the DB is:" (:mime-type res))
  (-> (response/response (:body res))
      (response/content-type (:mime-type res))))

(defn load-fn
  [uri]
  (if-let [res (first (db/get-document-by-uri uri))]
    (-> res
        (build-body)
        (make-response))))

(defn remove-fn
  [document]
  (db/delete-document! document))
