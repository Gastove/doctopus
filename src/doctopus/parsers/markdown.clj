(ns doctopus.markdown
  (:require [markdown.core :as md]))

(defn parse-mardown-file
  "Read a file and parse the contents from markdown to html"
  [file]
  {:pre [(re-find #"(?i)[md|markdown|mdown]$" file)]}
  (let [text-string (slurp file)]
    (md/md-to-html-string text-string)))

(defn parse-to-file
  "Parse a file and write the output string to a new file"
  [source-file destination-root])
