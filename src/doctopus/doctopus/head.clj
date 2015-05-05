(ns doctopus.doctopus.head
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [doctopus.configuration :refer [server-config]]
            [doctopus.db :as db]
            [doctopus.doctopus.tentacle :refer [map->Tentacle] :as t]
            [doctopus.shell :as sh]
            [me.raynes.fs :as fs]))

(defn perform-substitutions!
  "Given a map of {<term to look for> <replacement>} and a vector of words,
  returns a new vector with key replaced by val."
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
  (bootstrap-tentacles [this] [this subs-map])
  (list-tentacles [this subs-map])
  (load-tentacle-routes [this subs-map]))

(defrecord Head
    [name]
  HeadMethods
  (list-tentacles [this subs-map]
    (let [tentacle-maps (db/get-tentacles-for-head this)
          tentacle-configs (for [cfg-map tentacle-maps]
                             (parse-tentacle-config-map cfg-map subs-map))
          tentacles (map map->Tentacle tentacle-configs)]
      (doall tentacles)))
  (bootstrap-tentacles [this]
    (bootstrap-tentacles this {}))
  (bootstrap-tentacles [this subs-map]
    (let [tentacles (list-tentacles this subs-map)]
      (doseq [tentacle tentacles] (t/load-html tentacle))))
  (load-tentacle-routes [this subs-map]
    (into {} (for [tentacle (list-tentacles this subs-map)]
               (t/routes tentacle)))))
