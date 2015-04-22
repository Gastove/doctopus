(ns doctopus.storage
  "Defines how Doctopus speaks to different places it can store generated HTML"
  (:require [doctopus.storage-impls :as storage-impls]))

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
;; - Amazon S3
;;
;; At time of writing, I'm thinking of loading and saving one Tentacle worth of
;; documents all at once.

;; P. Sure I'm not using this
;; Control atom for Backend functions.
;;(def *backend* (atom "temp-fs"))
;; (defn load-documents [k] (load-from-storage @*backend* k))
;; (defn save-documents [k docs] (save-to-storage @*backend* k docs))

(defprotocol DoctopusBackend
  (set-backend! [this backend])
  (load-from-storage [this k])
  (save-to-storage [this k v]))

;; (defmulti set-backend!
;;   "Makes sure the Backend gets set only to a valid instance of the Backend class"
;;   class)

;; (defmethod set-backend! doctopus.files.storage.Backend
;;   [bkend]
;;   ())
;; (defmethod set-backend! :default)

;; The actual interface to a backend.
(defrecord Backend
    [backend available-backends]
  DoctopusBackend
  (set-backend! [this backend]
    (if (contains? (this :available-backends) backend)
      (assoc this :backend backend)
      (throw (java.lang.RuntimeException.
              (str "Cannot use declared storage backend: " backend)))))
  (load-from-storage [this k]
    (let [load-fn (get-in this [:backend :load-fn])]
      (load-fn k)))
  (save-to-storage [this k v]
    (let [save-fn (get-in this [:backend :save-fn])]
      (save-fn k v))))

(def available-backends {:temp-fs storage-impls/temp-fs-backend})

(def backend (Backend. storage-impls/temp-fs-backend available-backends))
