(ns doctopus.files.storage
  "Defines how Doctopus speaks to different places it can store generated HTML")

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

;; Control atom for Backend functions.
(def *backend* (atom "temp-fs"))

(defn load-documents [k] (load-from-storage @*backend* k))
(defn save-documents [k docs] (save-to-storage @*backend* k docs))

(defprotocol DoctopusBackend
  (load-from-storage [this k])
  (save-to-storage [this k v]))

(defmulti set-backend! class)
(defmethod set-backend! Backend
  [])
(defmethod set-backend! :default)

;; A name, a configuration map, a get function
(defrecord Backend
    [name conf load-fn save-fn]
  DoctopusBackend
  (load-from-storage [this k]
    (let [{:keys [conf load-fn]} this]
      (load-fn conf k)))
  (save-to-storage [this k v]
    (let [{:keys [conf save-fn]} this]
      (save-fn conf k))))
