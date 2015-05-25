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
  "Vector Triple of (path, directories, files), and function that filters
  relevant doc-types"
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

;; Yes yes yes. Super good.
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

;; Cannot remember why we did this.
(defn set-ext
  "For a given file with extension foo.bar, set extension to .html"
  [strang]
  (clojure.string/replace strang #"\.(\w+)$" ".html"))

;; Dig this.
(defn walk-docs-dir
  "Walk the docs; currently only looks for Markdown Documents."
  ([root] (walk-docs-dir root identity))
  ([root pred]
   (let [docs-all (fs/walk vector root) ;; Triple of directory path, directory name, file name
         filtered-docs (filter-files docs-all pred)
         assembled-result (assemble-file-filter-results root filtered-docs)]
     assembled-result)))

;; Almost certainly deprecated
(defn htmlify
  "Ingests a path. Turns the contents in to html, truncates the path, returns
  the pair."
  [path root html-fn]
  (let [html (html-fn (slurp path))
        truncated-path (truncate-str path root)
        htmlified-path (set-ext truncated-path)]
    [html htmlified-path]))

;; Probably also deprecated? Or, part of the md parser?
(defn write-doc
  "This is prooooooobably not how we wanna do this f'reals?"
  [[html rel-path] target-dir]
  (let [output-path (clojure.string/join "/" [target-dir rel-path])
        output-file (java.io.File. output-path)]
    (fs/create output-file)
    (spit output-file html)))


;; This cool dood needs to be changed to use the new storage backend...
;; eventually. Or it needs to be removed? Unclear. Maybe useful for the markdown
;; parser too.
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

;; Hrmmmmmmm. Could be useful for our markdown parser?
(defn read-html
  "Reads a directory structure looking for html; returns the result as a vector
  of relative path-html"
  [src-dir]
  (let [docs (walk-docs-dir src-dir html?)]
    (into [] (for [doc docs
                   :let [[html rel-path] (htmlify doc src-dir identity)]] ;; Is this a hack? WHY YES IT IS.
               [rel-path html]))))


(defn list-files-with-relative-paths
  "Given a directory, list the contents as pairs of:
  [path-relative-to-root file-handle]"
  [root]
  (let [all-files (walk-docs-dir root)]
    (for [found-file all-files
          :let [rel-path (truncate-str found-file root)]]
      [rel-path found-file])))
