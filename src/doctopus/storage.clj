(ns doctopus.storage
  "Defines how Doctopus speaks to different places it can store generated HTML"
  (:require [doctopus.configuration :refer [server-config]]
            [doctopus.storage.impls :as storage-impls]
            [taoensso.timbre :as log]
            [taoensso.timbre :as timbre]))

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
  (count-records-for-tentacle [this tent-name])
  (get-key-from-backend [this k])
  (load-from-storage [this k])
  (save-to-storage [this k v])
  (remove-from-storage [this k]))

;; The actual interface to a backend.
(defrecord Backend
    [backend available-backends]
  DoctopusBackend
  (count-records-for-tentacle [this tentacle]
    (let [c-fn (get-key-from-backend this :count-fn)]
      (c-fn tentacle)))
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
  (let [backends [storage-impls/postgres-backend]]
    (into {} (for [b backends] [(:name b) b]))))

(def default-backend :postgres)
(def backend (Backend. (atom (default-backend available-backends)) available-backends))

(defn set-backend! [backend-key]
  (if-let [new-backend (backend-key (:available-backends backend))]
    (swap! (:backend backend) (fn [_] new-backend))
    (throw (java.lang.RuntimeException.
            (str "Cannot use declared storage backend: " (name backend-key))))))
