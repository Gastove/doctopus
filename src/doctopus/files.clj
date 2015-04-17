(ns doctopus.files
  "Tools for managing files.

  We have: tools for walking a directory and returning only files
  matching a predicate.

  We probably want, but don't yet have: tools for managing local
  temp (making temp dirs, moving around any output generated, cleaning
  up later)."
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

(defn set-ext
  "For a given file with extension foo.bar, set extension to .html"
  [strang]
  (clojure.string/replace strang #"\.(\w+)$" ".html"))

(defn walk-the-docs
  "Walk the docs; currently only looks for Markdown Documents."
  [doc-root pred]
  (let [docs-all (fs/walk vector doc-root) ;; Triple of directory path, directory name, file name
        filtered-docs (filter-the-docs docs-all pred)
        assembled-result (assemble-the-results doc-root filtered-docs)]
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

(defn read-and-write-dir
  "Searches a source dir for all files that match a given predicate.

  Calls a provided function on each doc which will convert that doc to HTML.

  Writes the result in to a target directory, preserving relative file structure
  from the source-dir root."
  [src-dir target-dir type-pred html-fn]
  (let [docs-to-htmlify (walk-the-docs src-dir type-pred)]
    (doseq [doc docs-to-htmlify
            :let [html-relpath-pair (htmlify doc src-dir html-fn)]]
                                        ;(println "Here's one")
                                        ;(println html-relpath-pair)
      (write-doc html-relpath-pair target-dir)
      )))
