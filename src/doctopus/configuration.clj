(ns doctopus.configuration
  (:require [nomad :refer [defconfig]]
            [clojure.java.io :as io]))

(defconfig server-config (io/resource "configuration.edn"))

(defn- make-head-filename
  [head-name]
  (str "resources/config/heads/" head-name ".edn"))

(defn- make-tentacle-filename
  [tentacle-name]
  (str "resources/config/tentacle/" tentacle-name ".edn"))

(defn- read-from-disk
  [filename]
  (read-string (slurp filename)))

(defn- save-to-disk!
  [filename contents]
  (spit filename (pr-str contents)))

(defn save-head!
  "given a Doctopus Head map, save it to disk as an edn file in resources/"
  [doctopus-head]
  (let [head-name (:name doctopus-head)]
    (save-to-disk! (make-head-filename head-name) doctopus-head)))

(defn load-head
  [head-name]
  (read-from-disk (make-head-filename head-name)))

(defn remove-head!
  [head-name]
  (io/delete-file (make-head-filename head-name)))

(defn save-tentacle!
  "given a Doctopus Tentacle map, save it to disk as an edn file in resources/"
  [doctopus-tentacle]
  (let [tentacle-name (:name doctopus-tentacle)]
    (save-to-disk! (make-tentacle-filename tentacle-name) doctopus-tentacle)))

(defn load-tentacle
  [tentacle-name]
  (read-from-disk (make-tentacle-filename tentacle-name)))

(defn remove-tentacle!
  [tentacle-name]
  (io/delete-file (make-tentacle-filename tentacle-name)))
