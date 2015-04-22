(ns doctopus.shell
    (:require [clojure.java.shell/sh :as sh]))


(defn make-html
"This is a function that we use to shell out to a configured command
to build the HTML for a given project.

e.g. cd /to/dir; make html

TODO(gavin): what to do if output-dir is different from what we expect? 

Args:
    command: string, something that is within the $PATH of a shell.
    args: a list of strings.
    root-dir: string, path to the directory where the ``command`` should be run.
"
[command args root-dir]
    (apply sh (cons command args) :dir root-dir))
