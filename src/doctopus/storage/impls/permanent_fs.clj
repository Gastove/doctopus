(ns doctopus.storage.impls.permanent-fs
  (:require [clojure.string :as str]
            [doctopus.configuration :refer [docs-uri-prefix]]
            [doctopus
             [configuration :refer [server-config docs-uri-prefix]]
             [files :as files]]
            [doctopus.storage.impls.fs-impl :refer [save-html-file] :as fs-impl]
            [me.raynes.fs :as fs]
            [ring.util.mime-type :refer [ext-mime-type]]
            [ring.util.response :as response]
            [taoensso.timbre :as log]))

(def root (fs/file (:permanent-fs-root (server-config))))

(if (nil? root)
  (do (log/error "Configuration couldn't be correctly loaded. Most"
              "often, this is caused by an unset NOMAD_ENV"
              "environment variable; make sure NOMAD_ENV=dev,"
              "and try again. Until then, behavior may be very odd")))

(if-not (fs/exists? root) (fs/mkdirs root))

(defn count-fn [tentacle]
  (binding [fs/*cwd* (fs/file root docs-uri-prefix)]
    (let [target-dir (fs/file (:name tentacle))]
      (count (files/walk-docs-dir target-dir)))))

(defn save-fn
  "Save a dir of html stuff by moving it in to the annointed dir in
  the permanent fs"
  [key src-dir]
  (binding [fs/*cwd* (fs/file root docs-uri-prefix)]
    (let [target-dir (fs/file key)
          result (fs/copy-dir src-dir target-dir)]
      (and (fs/exists? result)
           (fs/readable? result)))))

(defn- build-response
  [file-handle]
  (let [body (slurp file-handle)
        file-name (.getPath file-handle)
        mime-type (ext-mime-type file-name)]
    (-> (response/response body)
        (response/content-type mime-type))))

(defn load-fn
  "Returns a file from storage if we have one, otherwise nil"
  [uri]
  (let [rel-path (str/replace-first uri  "/" "") ;; Remove the leading slash so we can treat as a relative path
        file-handle (binding [fs/*cwd* root] (fs/file rel-path))]
    (if (fs/exists? file-handle)
      (build-response file-handle)
      nil)))

(defn remove-fn
  [key]
  (log/info "Removing" key "from" root)
  (fs-impl/remove-html root key))
