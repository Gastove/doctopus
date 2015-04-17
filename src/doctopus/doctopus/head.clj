(ns doctopus.doctopus.head)


;; Head
(defprotocol HeadMethods
  (list-tentacles [this]))

(defrecord Head
    [tentacles]
  HeadMethods
  (list-tentacles [this] (:tentacles this)))
