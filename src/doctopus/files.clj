(ns doctopus.files
  (:require [me.raynes.fs :as fs]
            [clojure.string :as str]))

;; THIS IS TOTALLY DATABASS
;; (pprint (fs/walk vector "/Users/rossdonaldson/Code/doctopus/src/"))

(defmulti is-type?
  "Returns true if the type of the file matches the type given."
  (fn [fobj regex] (class fobj)))

(defmethod is-type? java.lang.String
  [file-string type-regex]
  (if (re-find type-regex file-string)
    true
    false
    ))

(defmethod is-type? java.io.File
  [file-handle type-regex]
  (is-type? (.getPath file-handle) type-regex))


(defn markdown?
  "returns true only for type files. Expects a Java.Util.FileHandle"
  [file-handle]
  (is-type? file-handle #"(?i)md|markdown|mdown$"
            ))


(defn restructured-text?
  "returns true only for type files. Expects a Java.Util.FileHandle"
  [file-handle]
  (is-type? #"(?i)rst|rest$" (.getPath file-handle)
            ))


(defn filter-the-docs
  "Vector Triple of (path, directories, files), and function that filters relevant doc-types"
  [[path directories files] doc-type-fn]
  (println directories)
  (if-let [matched-files (filter doc-type-fn files)]
    [path matched-files]
    nil))


(declare truncate-str)
(defn assemble-the-results
  "More stuff"
  [doc-root [path matched-files]]
  (let [str-path (.getPath path)
        str-rel-path (truncate-str doc-root str-path)
        ]
    (for [matched-file matched-files]
      [doc-root str-rel-path matched-file])
    ))


(defn truncate-str
  "Removes non-relatice path data from a string"
  [fq-path rel-path]
  (str/replace fq-path rel-path ""))


(defn walk-the-docs
  "Walk the docs; currently only looks for Markdown Documents."
  [doc-root]
  (let [docs-all (fs/walk vector doc-root) ;; Triple of directory path, directory name, file name
        filtered-docs (filter-the-docs docs-all markdown-type)
        assembled-result (assemble-the-results doc-root filtered-docs)
        ]
    assembled-result
    ))
