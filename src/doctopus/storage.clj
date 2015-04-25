(ns doctopus.storage
  "Defines how Doctopus speaks to different places it can store generated HTML"
  (:require [doctopus.storage.impls :as storage-impls]))

;; ## Storage
;; Doctopus shouldn't really care _where_ it puts generated HTML; it
;; just needs a consistent interface. Thus, the Backend: a way of
;; generalizing and controlling where Doctopus tries to get and put Stuff.
;;
;; As a first pass, the backend will be defined by an atom which
;; contains the current Backend instance; functions in this NS will
;; work against that atom. This means a Doctopus will have a single Backend; we
;; can potentially expand this later.
;;
;; Initial backends:
;; - Temporary filesystem (provides it's own directory)
;; - Permanent filesystem (needs a directory provided)
;; - Amazon S3 (maybe?)
;;
;; At time of writing, I'm thinking of loading and saving one Tentacle worth of
;; documents all at once.

(defprotocol DoctopusBackend
  (set-backend! [this backend])
  (get-key-from-backend [this k])
  (load-from-storage [this k])
  (save-to-storage [this k v])
  (remove-from-storage [this k]))

;; The actual interface to a backend.
(defrecord Backend
    [backend available-backends]
  DoctopusBackend
  (set-backend! [this backend-key]
    (if-let [new-backend (backend-key (:available-backends this))]
      (swap! (:backend this) (fn [_] new-backend))
      (throw (java.lang.RuntimeException.
              (str "Cannot use declared storage backend: " (name backend))))))
  (get-key-from-backend [this k]
    (let [retrieved-backend (deref (:backend this))]
      (k retrieved-backend)))
  (load-from-storage [this k]
    (let [load-fn (get-key-from-backend this :load-fn)]
      (load-fn k)))
  (save-to-storage [this k v]
    (let [save-fn (get-key-from-backend this :save-fn)]
      (save-fn k v)))
  (remove-from-storage [this k]
    (let [remove-fn (get-key-from-backend this :remove-fn)]
      (remove-fn k))))

(def available-backends
  (let [backends [storage-impls/temp-fs-backend
                  storage-impls/permanent-fs-backend]]
    (into {} (for [b backends] [(:name b) b]))))

(def default-backend :permanent-fs)
(def backend (Backend. (atom (default-backend available-backends)) available-backends))
