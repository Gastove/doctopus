(ns doctopus.doctopus.head
  (:require [clojure.edn :as edn]
            [me.raynes.fs :as fs]
            [doctopus.doctopus.tentacle :refer [map->Tentacle] :as t]))


;; Head
(defprotocol HeadMethods
  (bootstrap-tentacles [this root])
  (list-tentacles [this])
  (load-tentacle-routes [this]))

(defrecord Head
    [name]
  HeadMethods
  (bootstrap-tentacles [this root]
    (let [tentacles-root (binding [fs/*cwd* root] (fs/file (:name this)))
          tentacle-files (fs/list-dir tentacles-root)
          tentacle-configs (for [tf tentacle-files
                                 :let [strang (slurp tf)]]
                             (edn/read-string strang))
          tentacles (map #(map->Tentacle %) tentacle-configs)]
      ;(doseq [tentacle tentacles] (t/generate-html tentacle))
      (assoc this :tentacles tentacles)))
  (list-tentacles [this] (:tentacles this))
  (load-tentacle-routes [this]
    (into {} (for [tentacle (:tentacles this)]
               (t/routes tentacle)))))
