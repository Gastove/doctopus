(ns doctopus.doctopus
  "A Doctopus represents the entire, wriggling, living creature that is your
  documentation. A Doctopus is a singluar abstraction -- at time of writing, you
  can only have one. A Doctopus contains all of the configuration necessary for
  your docs to make it to the web.

  Each Doctopus will have one or more Heads, which represent a logical grouping
  of docs, and some number of Tentacles -- individual sources of documentation
  orchestrated by the Head. "
  (:require [compojure.core :refer [context routes]]
            [doctopus.db :as db]
            [doctopus.doctopus.head :as h]
            [doctopus.doctopus.tentacle :as t]
            [me.raynes.fs :as fs])
  (:import [doctopus.doctopus.head Head]))

;; Here there be Peculiar Metaphors for Information Dissemination
(defprotocol DoctopusMethods
  (bootstrap-heads [this])
  (load-routes [this])
  (list-heads [this])
  (list-tentacles [this])
  (list-tentacles-by-head [this head]))

(defrecord Doctopus
    [configuration substitutions]
  DoctopusMethods
  (bootstrap-heads [this]
    (let [heads (map h/map->Head (db/get-all-heads))]
      (doall (map #(h/bootstrap-tentacles % substitutions) heads))))
  (list-heads [this] (doall (map h/map->Head (db/get-all-heads))))
  (list-tentacles [this]
    (into [] (flatten (for [head (list-heads this)
                            :let [tentacles (h/list-tentacles head (:substitutions this))]]
                        tentacles))))
  (list-tentacles-by-head [this head]
    (map #(h/list-tentacles % (:substitutions this))
         (filter #(= head (:name %)) (list-heads this))))
  (load-routes [this]
    (apply routes
           (for [head (list-heads this)
                 :let [substitutions (:substitutions this)]]
             (h/load-tentacle-routes head substitutions)))))
