(ns doctopus.shell
  (:require [clojure.java.shell :as sh]
            [clojure.string :as str]))

(defn make-html
  "This is a function that we use to shell out to a configured command
  to build the HTML for a given project.

  e.g. cd /to/dir; make html

  Args:
  command: string, something that is within the $PATH of a shell.
  args: a list of strings.
    root-dir: string, path to the directory where the ``command`` should be run. "
  ([command]
   (sh/sh command))
  ([command args]
   (apply sh/sh (cons command args)))
  ([command args root-dir]
   (apply sh/sh (cons command (conj args :dir root-dir)))))

(defn git-clone
  [repo dest]
  (let [res (sh/sh "git" "clone" repo dest)
        status (:exit res)]
    (= 0 status)))

(defn split
  [string]
  (str/split string #" "))

(defn injest-shell-vector
  "Takes a vector of shell commands, like:

  [\"npm i\"
  \"node bin/mop.js\"]

  And returns a vector of vectors, like:

  [[\"npm\" \"i\"]
  [\"node\" \"bin/mop.js\"]]

  Note that to work, this _must_ preserve command order in all regards!"
  [incoming-vec]
  (->> incoming-vec
       (map split)))
