(ns doctopus.files
  "Tools for managing files.

We have: tools for walking a directory and returning only files matching a predicate.

  We probably want, but don't yet have: tools for managing local temp (making
  temp dirs, moving around any output generated, cleaning up later)."
  (:require [clojure.string :as str]
            [doctopus.files.predicates :refer [markdown?]]
            [me.raynes.fs :as fs]))

;; I think we've pretty much captured this by now? -- RMD Sun Mar 15 20:37:46 2015

;; THIS IS TOTALLY DATABASS
;; (pprint (fs/walk vector "/Users/rossdonaldson/Code/doctopus/src/"))

(defn filter-the-docs
  "Vector Triple of (path, directories, files), and function that filters relevant doc-types"
  [path-listing doc-type-fn]
  (into [] (for [[path directory files] path-listing
                 :let [found-files (filter doc-type-fn files)]
                 :when (not-empty found-files)]
             [path found-files])))


(declare truncate-str)
(defn assemble-the-results
  "More stuff"
  [doc-root path-and-files]
  (flatten (for [[path matched-files] path-and-files]
             (for [matched-file matched-files]
               (fs/file path matched-file)))))


(defn truncate-str
  "Given a fully qualified root directory, correctly return the path, relative
  to a given root. I.E: given /foo/bar/baz and root baz, return /;
  given /foo/bar/baz and root bar, return /baz."
  [fq-path root-path]
  (let [fq-path-string (.toString fq-path)
        root-path-string (.toString root-path)
        splitting-regex (re-pattern root-path-string)
        ending-regex (re-pattern (str root-path-string "$"))]
    (if (re-find ending-regex fq-path-string)
      "/"
      (last (clojure.string/split fq-path-string splitting-regex)))))

(defn walk-the-docs
  "Walk the docs; currently only looks for Markdown Documents."
  [doc-root]
  (let [docs-all (fs/walk vector doc-root) ;; Triple of directory path, directory name, file name
        filtered-docs (filter-the-docs docs-all markdown?)
        assembled-result (assemble-the-results doc-root filtered-docs)]
    assembled-result
    ))
