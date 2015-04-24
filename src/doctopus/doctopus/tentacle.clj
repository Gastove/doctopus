(ns doctopus.doctopus.tentacle
  (:require [doctopus.shell :refer [make-html git-clone]]
            [doctopus.storage :refer [save-to-storage load-from-storage backend]]
            [me.raynes.fs :as fs]
            [taoensso.timbre :as log]))

(defn- get-source
  [sc-location dest]
  (git-clone sc-location dest))

;; TENTACLES
(defprotocol TentacleMethods
  (generate-html [this])
  (save-build-output [this dir])
  (routes [this]))

(defrecord Tentacle
    [name html-command html-args output-root source-location]
  TentacleMethods
  (generate-html [this]
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
  (routes [this]
    (load-from-storage backend (:name this))))


;; ### Tentacle Config Spec
(def one-tentacle (map->Tentacle {:src-root "/tmp/doctopus"
                                  :output-root "/tmp/doctopus/docs"
                                  :html-command "lein"
                                  :html-args ["marg" "-f" "index.html"]
                                  :name "doctopus"}))
