(ns doctopus.util)

(enable-console-print!)

(defn get-value
  [event]
  (-> event .-target .-value))

(defn http-ok?
  [status-code]
  (and (> status-code 199) (< status-code 299)))

(defn redirect-to
  [new-url]
  (set! js/window.location.href new-url))

(defn in?
  [item coll]
  (not (not-any? #(= item %) coll)))

(defn maybe-conj
  "If pred passes, return (conj coll x), else return coll"
  [pred coll x]
  (if pred (conj coll x) coll))
