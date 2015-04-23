(ns doctopus.doctopus.tentacle
  (:require [doctopus.storage :refer [save-to-storage load-from-storage backend]]
            [doctopus.shell :refer [make-html]]))


;; TENTACLES
(defprotocol TentacleMethods
  (generate-html [this])
  (save-build-output [this])
  (routes [this]))

(defrecord Tentacle
    [name html-command html-args src-root output-root]
  TentacleMethods
  (generate-html [this]
    (let [{:keys [html-command html-args src-root]} this]
      (apply make-html [html-command html-args src-root])))
  (save-build-output [this]
    (let [{:keys [name output-root]} this])
    (save-to-storage backend name output-root))
  (routes [this]
    (load-from-storage backend (:name this))))


;; ### Tentacle Config Spec
(def one-tentacle (map->Tentacle {:src-root "/tmp/doctopus"
                                  :output-root "/tmp/doctopus/docs"
                                  :html-command "lein"
                                  :html-args ["marg" "-f" "index.html"]
                                  :name "doctopus"}))
