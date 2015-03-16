(ns doctopus.files.predicates
  "We've got a loooot of filtering to do.

  Like, a lot.")

(defmulti is-type?
  "Returns true if the type of the file matches the type given."
  (fn [fobj regex] (class fobj)))

(defmethod is-type? java.lang.String
  [file-string type-regex]
  (if (re-find type-regex file-string)
    true
    false))

(defmethod is-type? java.io.File
  [file-handle type-regex]
  (is-type? (.getPath file-handle) type-regex))


(defn markdown?
  "returns true only for type files."
  [file-handle]
  (is-type? file-handle #"(?i)md|markdown|mdown$"
            ))


(defn restructured-text?
  "returns true only for type files."
  [file-handle]
  (is-type? #"(?i)rst|rest$" (.getPath file-handle)
            ))
