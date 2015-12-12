(ns doctopus.doctopus.tentacle
  "A `Tentacle' defines a single unit of documentation -- one 'source' worth,
  and all the configs and tooling required to build it."
  (:require [clojure.string :as str]
            [compojure.core :refer [GET routes]]
            [doctopus
             [configuration :as configuration :refer [docs-uri-prefix]]
             [db :as db]
             [files :as f]
             [shell :refer [git-clone make-html-from-vec]]]
            [me.raynes.fs :as fs]
            [ring.util
             [mime-type :as ring-mime]
             [response :as response]]
            [taoensso.timbre :as log])
  (:import java.io.ByteArrayInputStream))

;; To match against mimetype strings
(def is-img-regex #"^image")

;; Utilities for marking HTTP Resposnes
(defn- assign-body-and-image
  [base-doc doc-file mime-type]
  (log/debug base-doc)
  (let [doc-content (slurp doc-file)]
    (if (re-find is-img-regex mime-type)
      (assoc base-doc :body "image" :image (.getBytes doc-content))
      (assoc base-doc :body doc-content :image nil))))

(defn- build-body
  "Examine a DB result to see if it's an image; if so, build a ByteArrayInputStream
  and use it as the response body."
  [res]
  (if (= "image" (:body res))
    (let [bais (ByteArrayInputStream. (:image res))]
      (assoc res :body bais))
    res))

(defn- make-response
  [res]
  (-> (response/response (:body res))
      (response/content-type (:mime-type res))))

(defn- load-fn
  [uri]
  (if-let [res (first (db/get-document-by-uri uri))]
    (-> res
        (build-body)
        (make-response))))

;; Acquiring repos
(defn- get-source
  "We may eventually support more ways of getting source! Would be nice. Not
  today."
  [sc-location dest]
  (git-clone sc-location dest))

;; Storing generated HTML
(defn- save-to-database
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
                            :uri (str "/" configuration/docs-uri-prefix "/" tent-name relpath)
                            :tentacle-name tent-name}]]
      (if (nil? doc-mime-type)
        (log/warn "Refusing to save" relpath ", cannot discern mime-type")
        (let [new-doc (assign-body-and-image base-doc doc-file doc-mime-type)]
          (log/debug "Reading doc from:" relpath)
          (log/debug "Giving doc URI:" (:uri new-doc))
          (db/save-document! new-doc))))
    (db/update-document-index)))

;; #### "Try, report, return"-ing
;; I've found myself doing a lot of this:
;; (if (do-a-thing-that-returns-something-or-nil)
;;     (do (log the result) true)
;;     (do (log the failure) nil))

(defn report-success
  [msg]
  (log/info msg)
  true)

(defn report-error
  [msg]
  (log/error msg)
  nil)

(defn check-and-report
  [result noun success-msg fail-msg]
  (let [success-tpl "Success: %s for %s"
        error-tpl   "Couldn't %s for %s"]
    (if result
      (report-success (format success-tpl success-msg noun))
      (report-error (format error-tpl fail-msg noun)))))

;; TENTACLES
(defprotocol TentacleMethods
  (load-html [this] "Makes sure the HTML has been generated for this tentacle")
  (generate-html [this]) ;; check
  (save-build-output [this dir]) ;; check
  (get-html-entrypoint [this]) ;; check
  (generate-routes [this]) ;; check
  (count-records [this])) ;; check

(defrecord Tentacle
    [name html-commands output-root source-location entry-point]
  TentacleMethods
  (load-html [this]
    (if (= 0 (count-records this))
      (do (log/info "No HTML found for" (:name this))
          (generate-html this))
      (log/info "Found html for" (:name this))))
  (generate-html [this]
    (log/info "Generating HTML for" (:name this))
    (let [{:keys [html-commands source-location output-root]} this
          target-dir (fs/temp-dir "doctopus-clone")
          success? (get-source source-location (.getPath target-dir))]
      (if success?
        (do (binding [fs/*cwd* target-dir]
              (let [html-dir (fs/file output-root)]
                (make-html-from-vec html-commands target-dir)
                (check-and-report
                 (save-build-output this html-dir) name "generated HTML" "generate HTML"))))
        (report-error "Couldn't clone source! Argggggg!"))))
  (save-build-output [this dir]
    (let [{:keys [name]} this]
      (check-and-report
       (save-to-database name dir) name "saved HTML" "save HTML")))
  (get-html-entrypoint [this]
    (str/join "/" ["" docs-uri-prefix (:name this) (:entry-point this)]))
  (generate-routes [this]
    (routes
     (GET "*" {:keys [uri]}
          (if-let [res (first (db/get-document-by-uri uri))]
            (-> res
                (build-body)
                (make-response))))))
  (count-records [this] (count (db/get-all-documents-for-tentacle this))))
