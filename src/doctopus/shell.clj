(ns doctopus.shell
  (:require [clojure.java.shell :as sh]
            [clojure.string :as str]
            [doctopus.configuration :refer [server-config]]))

(defn make-html
  "Shell out a command in a specific dir.

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

(defn make-html-from-vec
  "Given a vector of vectors of shell commands, like:

  [['do' 'one' 'thing]
  ['do' 'the' 'next' 'thing]]

  And a dir to execute them in, execute each one in turn."
  [cmd-vec working-dir]
  (doseq [cmd cmd-vec
          :let [full-cmd (conj cmd :dir working-dir)]]
    (apply sh/sh full-cmd)))

(defn git-clone
  "Clones a git repo to a known destination."
  [repo dest]
  (let [res (sh/sh "git" "clone" repo dest)
        status (:exit res)]
    (= 0 status)))
