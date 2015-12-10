(ns doctopus.storage.impls.permanent-fs
  (:require [clojure.string :as str]
            [doctopus.configuration :refer [docs-uri-prefix]]
            [doctopus
             [configuration :refer [server-config]]
             [files :as files]]
            [doctopus.storage.impls.fs-impl :refer [save-html-file] :as fs-impl]
            [me.raynes.fs :as fs]
            [ring.util.response :refer [file-response]]
            [taoensso.timbre :as log]))

(def root (fs/file (:permanent-fs-root (server-config))))

(if (nil? root)
  (do (log/error "Configuration couldn't be correctly loaded. Most"
              "often, this is caused by an unset NOMAD_ENV"
              "environment variable; make sure NOMAD_ENV=dev,"
              "and try again. Until then, behavior may be very odd")))

(if-not (fs/exists? root) (fs/mkdirs root))

;; TODO:
(defn count-fn [] nil)

(defn save-fn
  "Save a dir of html stuff by moving it in to the annointed dir in
  the permanent fs"
  [key src-dir]
  (binding [fs/*cwd* root]
    (let [target-dir (fs/file key)
          result (fs/copy-dir src-dir target-dir)]
      (and (fs/exists? result)
           (fs/readable? result)))))

(defn load-fn
  "Returns a file from storage if we have one, otherwise nil"
  [uri]
  (let [rel-path (str/replace uri (str docs-uri-prefix "/") "") ;; Remove URI prefix to get relative path
        file-handle (binding [fs/*cwd* root] (fs/file rel-path))
        file-name (.getPath file-handle)]
    (if (fs/exists? file-handle)
      (file-response file-name)
      nil)))

(defn remove-fn
  [key]
  (log/info "Removing" key "from" root)
  (fs-impl/remove-html root key))
