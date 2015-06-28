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

(defn not-in?
  [item coll]
  (not-any? #(= item %) coll))
