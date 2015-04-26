(ns doctopus.doctopus
  "A Doctopus represents the entire, wriggling, living creature that is your
  documentation. A Doctopus is a singluar abstraction -- at time of writing, you
  can only have one. A Doctopus contains all of the configuration necessary for your docs to make it to the web.

  Each Doctopus will have one or more Heads, which represent a logical grouping
  of docs, and some number of Tentacles -- individual sources of documentation
  orchestrated by the Head. "
  (:require [doctopus.doctopus.head :as h]
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
    [configuration]
  DoctopusMethods
  (bootstrap-heads [this]
    (let [heads-dir (:heads-dir configuration)
          dirs (fs/list-dir heads-dir)
          head-names (map #(.getName %) dirs)
          heads (map #(h/Head. %) head-names)]
      (assoc this :heads (doall (map #(h/bootstrap-tentacles % heads-dir) heads)))))
  (list-heads [this] (:heads this))
  (list-tentacles [this]
    (into [] (for [head (:heads this)
                   :let [tentacles (h/list-tentacles head)]]
               [tentacles])))
  (list-tentacles-by-head [this head]
    (map h/list-tentacles (filter #(= head (:name %)) (:heads this))))
  (load-routes [this]
    ;; You may be wondering, "what the hell is going on here". And that's a
    ;; great question! The answer is: we need to make sure that what's returned
    ;; is a map from the key "/" to a map of the form {tentacle-name
    ;; tentacle-routes}. This is not the prettiest, but it does just that.
    {"/" (apply merge (flatten (into [] (for [head (:heads this)
                                  :let [route-map (h/load-tentacle-routes head)]]
                              [route-map]))))}))
