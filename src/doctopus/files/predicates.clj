(ns doctopus.files.predicates
  "We've got a loooot of filtering to do.

  Like, a lot.")

;; ### The `is-type' Multimethod
;; Dispatches on the type of an object which represents a file; returns a string
;; representation of the fully qualified path of that file, suitable for
;; regexing

(defmulti is-type?
  "Returns true if the type of the file matches the type regex provided."
  (fn [fobj regex] (class fobj)))

(defmethod is-type? java.lang.String
  [file-string type-regex]
  (if (re-find type-regex file-string)
    true
    false))

(defmethod is-type? java.io.File
  [file-handle type-regex]
  (is-type? (.getPath file-handle) type-regex))

;; ### File Predicates
;; Used for checking that an object representing a file -- whether it's a String
;; or a java.io.File -- has a particular extension

(defn markdown?
  [file-handle]
  (is-type? file-handle #"(?i)md|markdown|mdown$"))

(defn restructured-text?
  [file-handle]
  (is-type? file-handle #"(?i)rst|rest$"))

(defn html?
  [file-handle]
  (is-type? file-handle #"(?i)html$"))
