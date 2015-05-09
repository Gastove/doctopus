(ns doctopus.storage.impls.permanent-fs
  "Implements a kind of key/value storage on a machine's permanent filesystem,
  given a root directory to write to. The sub-directories within the root are
  the 'keys', their contents, the 'values'"
  (:require [bidi.ring :as bidi-ring]
            [doctopus
             [configuration :refer [server-config]]
             [files :as files]]
            [doctopus.storage.impls.fs-impl :refer [save-html-file] :as fs-impl]
            [me.raynes.fs :as fs]
            [taoensso.timbre :as log]))

(def root (fs/file (:permanent-fs-root (server-config))))

(if (nil? root)
  (do (log/error "Configuration couldn't be correctly loaded. Most"
              "often, this is caused by an unset NOMAD_ENV"
              "environment variable; make sure NOMAD_ENV=dev,"
              "and try again. Until then, behavior may be very odd")))

(if-not (fs/exists? root) (fs/mkdirs root))


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
  "Returns the routes this serves"
  [key]
  (let [file-handle (binding [fs/*cwd* root] (fs/file key))
        file-name (str (.getPath file-handle) "/")]
    (if (fs/exists? file-handle)
      [key {"/" (bidi-ring/->Files {:dir file-name})}]
      nil)))

(defn remove-fn
  [key]
  (log/info "Removing" key "from" root)
  (fs-impl/remove-html root key))
