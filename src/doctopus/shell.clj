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

(defn git-clone
  [repo dest]
  (let [res (sh/sh "git" "clone" repo dest)
        status (:exit res)]
    (= 0 status)))

;; I'm not 100% convinced this is the best place for this stuff to go, but I
;; don't presently have any better ideas. -- RMD

;; A map from string-to-look-for to value-to-sub-with
(def substitutions {"$URL_ROOT"  (:ip (server-config))})

(defn perform-substitutions!
  [subs-map cmd-vec]
  (letfn [(substitute [word] (if (contains? subs-map word)
                               (subs-map word)
                               word))]
    (map substitute cmd-vec)))

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

  Note that to work, this _must_ preserve command order in all regards! Also,
  this should *always* return a vector-of-vectors."
  ([incoming-vec] (injest-shell-vector incoming-vec {}))
  ([incoming-vec subs-map]
   (->> incoming-vec
        (map split)
        (perform-substitutions! subs-map))))
