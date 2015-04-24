(ns doctopus.doctopus.tentacle
  (:require [clojure.string :as str]
            [doctopus.shell :refer [make-html git-clone]]
            [doctopus.storage :refer [save-to-storage load-from-storage backend]]
            [me.raynes.fs :as fs]
            [taoensso.timbre :as log]))

(defn- get-source
  [sc-location dest]
  (git-clone sc-location dest))

;; TENTACLES
(defprotocol TentacleMethods
  (load-html [this])
  (generate-html [this])
  (save-build-output [this dir])
  (get-html-entrypoint [this])
  (routes [this]))

(defrecord Tentacle
    [name html-command html-args output-root source-location entry-point]
  TentacleMethods
  (load-html [this]
    (if (nil? (load-from-storage backend (:name this)))
      (generate-html this)
      (log/info "Found html for" (:name this))))
  (generate-html [this]
    (log/info "Generating HTML for" (:name this))
    (let [{:keys [html-command html-args source-location output-root]} this
          target-dir (fs/temp-dir "doctopus-clone")
          success? (get-source source-location (.getPath target-dir))]
      (if success?
        (do (binding [fs/*cwd* target-dir]
              (let [html-dir (fs/file output-root)]
                (apply make-html [html-command html-args (.getPath target-dir)])
                (save-build-output this html-dir)
                (log/info "Generated HTML for" name))))
        (log/error "Couldn't clone source! Argggggg!"))))
  (save-build-output [this dir]
    (let [{:keys [name]} this])
    (save-to-storage backend name dir))
  (get-html-entrypoint [this]
    (str/join "/" ["" "docs" (:name this) (:entry-point this)]))
  (routes [this]
    (load-from-storage backend (:name this))))
