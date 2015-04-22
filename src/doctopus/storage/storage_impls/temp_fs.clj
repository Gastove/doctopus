(ns doctopus.storage-impls.temp-fs
  (:require [clojure.string :as str]
            [doctopus.files :as files]
            [doctopus.files.storage-impls :refer :all]
            [doctopus.storage.storage-impls.fs-impl :refer [save-html-file]]
            [me.raynes.fs :as fs]))

;; The root of the temp filesystem. Each Thing will store its stuff
;; within this directory
(def temp-dir (atom (fs/temp-dir  "doctopus-temp")))

(defn regenerate-temp-dir
  "Generates a new temp dir"
  []
  (swap! temp-dir fs/temp-dir "doctopus-temp"))

(defn save-fn
  [key path-html-pairs]
  (doseq [[path html] path-html-pairs] (save-html-file @temp-dir key path html)))

(defn load-fn
  "This is as Version 1 a way of doing this as I can think of:

  1. We load html
  2. We wrap that html in a function that returns itself, so we can Bidi"
  [key]
  (binding [fs/*cwd* @temp-dir]
    (let [dir (fs/file key)
          rel-path-html-pairs (files/read-html dir)]
      (into [] (map (fn [[rel-path html]] [rel-path (fn [_] html)]) rel-path-html-pairs)))))
