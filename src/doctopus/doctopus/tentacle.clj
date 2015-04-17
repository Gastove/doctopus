(ns doctopus.doctopus.tentacle)


;; TENTACLES
(defprotocol TentacleMethods)

(defrecord Tentacle
    []
  TentacleMethods
  )
