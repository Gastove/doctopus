(ns doctopus.doctopus.head
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [doctopus.configuration :refer [server-config]]
            [doctopus.doctopus.tentacle :refer [map->Tentacle] :as t]
            [doctopus.shell :as sh]
            [me.raynes.fs :as fs]))

(defn perform-substitutions!
  [subs-map cmd-vec]
  (letfn [(substitute [word] (if (contains? subs-map word)
                               (subs-map word)
                               word))]
    (map substitute cmd-vec)))

(defn split
  [string]
  (str/split string #" "))

(defn injest-shell-strings
  "Takes a vector of shell commands, like:

  [\"npm i\"
  \"node bin/mop.js\"]

  And returns a vector of vectors, like:

  [[\"npm\" \"i\"]
  [\"node\" \"bin/mop.js\"]]

  Note that to work, this _must_ preserve command order in all regards! Also,
  this should *always* return a sequence of vectors."
  ([incoming-vec] (injest-shell-strings incoming-vec {}))
  ([incoming-vec subs-map]
   (->> incoming-vec
        (map split)
        (perform-substitutions! subs-map)
        (vec))))

(defn parse-tentacle-config-map
  ([unparsed-cfgs] (parse-tentacle-config-map unparsed-cfgs {}))
  ([unparsed-cfgs subs-map]
   (let [unparsed-cmd-string (:html-commands unparsed-cfgs)
         parsed-cmd-vecs (injest-shell-strings unparsed-cmd-string subs-map)]
     (assoc unparsed-cfgs :html-commands parsed-cmd-vecs))))

;; Head
(defprotocol HeadMethods
  (bootstrap-tentacles [this root] [this root subs-map])
  (list-tentacles [this])
  (load-tentacle-routes [this]))

(defrecord Head
    [name]
  HeadMethods
  (bootstrap-tentacles [this root]
    (bootstrap-tentacles this root {}))
  (bootstrap-tentacles [this root subs-map]
    (let [tentacles-root (binding [fs/*cwd* root] (fs/file (:name this)))
          tentacle-files (fs/list-dir tentacles-root)
          tentacle-configs (for [tf tentacle-files
                                 :let [strang (slurp tf)
                                       unparsed-map (edn/read-string strang)]]
                             (parse-tentacle-config-map unparsed-map subs-map))
          tentacles (map #(map->Tentacle %) tentacle-configs)]
      (doseq [tentacle tentacles] (t/load-html tentacle))
      (assoc this :tentacles tentacles)))
  (list-tentacles [this] (:tentacles this))
  (load-tentacle-routes [this]
    (into {} (for [tentacle (:tentacles this)]
               (t/routes tentacle)))))
