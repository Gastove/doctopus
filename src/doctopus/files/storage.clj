(ns doctopus.files.storage
  "Create a common interface to storage backends.

  It shouldn't matter how a particular org wants to provide storage for
  doctopus; the backend should be extensible, even if we provide very
  few default implementations.

  Initial backends:
  - Temporary filesystem (provides it's own directory)
  - Permanent filesystem (needs a directory provided)
  - Amazon S3")

(def *backend* (atom "temp-fs"))

(defn set-backend!
  [new-backend]
  nil)

(defrecord Backend)
