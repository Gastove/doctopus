(ns doctopus.shell
  (:require [clojure.java.shell :as sh]
            [clojure.string :as str]
            [doctopus.configuration :refer [server-config]]))

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

(defn make-html-from-vec
  [cmd-vec working-dir]
  (doseq [cmd cmd-vec
          :let [full-cmd (conj cmd :dir working-dir)]]
    (apply sh/sh full-cmd)))

(defn git-clone
  [repo dest]
  (let [res (sh/sh "git" "clone" repo dest)
        status (:exit res)]
    (= 0 status)))
