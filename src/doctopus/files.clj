(ns doctopus.files
  "Tools for managing files.

  We have: tools for walking a directory and returning only files
  matching a predicate.

  We probably want, but don't yet have: tools for managing local
  temp (making temp dirs, moving around any output generated, cleaning
  up later)."
  (:require [clojure.string :as str]
            [doctopus.files.predicates :refer [markdown? html?]]
            [me.raynes.fs :as fs]))


(defn filter-files
  "Vector Triple of (path, directories, files), and function that filters relevant doc-types"
  [path-listing doc-type-fn]
  (into [] (for [[path directory files] path-listing
                 :let [found-files (filter doc-type-fn files)]
                 :when (not-empty found-files)]
             [path found-files])))


(declare truncate-str)
(defn assemble-file-filter-results
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

(defn set-ext
  "For a given file with extension foo.bar, set extension to .html"
  [strang]
  (clojure.string/replace strang #"\.(\w+)$" ".html"))

(defn walk-docs-dir
  "Walk the docs; currently only looks for Markdown Documents."
  [doc-root pred]
  (let [docs-all (fs/walk vector doc-root) ;; Triple of directory path, directory name, file name
        filtered-docs (filter-files docs-all pred)
        assembled-result (assemble-file-filter-results doc-root filtered-docs)]
    assembled-result
    ))

(defn htmlify
  "Ingests a path. Turns the contents in to html, truncates the path, returns
  the pair."
  [path root html-fn]
  (let [html (html-fn (slurp path))
        truncated-path (truncate-str path root)
        htmlified-path (set-ext truncated-path)]
    [html htmlified-path]))

(defn write-doc
  "This is prooooooobably not how we wanna do this f'reals?"
  [[html rel-path] target-dir]
  (let [output-path (clojure.string/join "/" [target-dir rel-path])
        output-file (java.io.File. output-path)]
    (fs/create output-file)
    (spit output-file html)))


;; This cool dood needs to be changed to use the new storage
;; backend... eventually. Or it needs to be removed? Unclear.
(defn read-and-write-dir
  "Searches a source dir for all files that match a given predicate.

  Calls a provided function on each doc which will convert that doc to HTML.

  Writes the result in to a target directory, preserving relative file
  structure from the source-dir root."
  [src-dir target-dir type-pred html-fn]
  (let [docs-to-htmlify (walk-docs-dir src-dir type-pred)]
    (doseq [doc docs-to-htmlify
            :let [html-relpath-pair (htmlify doc src-dir html-fn)]]
      (write-doc html-relpath-pair target-dir)
      )))

(defn read-html
  "Reads a directory structure looking for html; returns the result as a vector
  of relative path-html"
  [src-dir]
  (let [docs (walk-docs-dir src-dir html?)]
    (into [] (for [doc docs
           :let [html-relpath-pair (htmlify doc src-dir identity)]] ;; Is this a hack? WHY YES IT IS.
               html-relpath-pair))))
