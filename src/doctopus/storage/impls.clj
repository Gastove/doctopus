(ns doctopus.storage.impls
  (:require [doctopus.storage.impls.temp-fs :as temp-fs-impl]
            [doctopus.storage.impls.permanent-fs :as perm-fs-impl]))

;; ## Backend Implementations
;; A backend implementation needs to expose two functions: one for load, and one
;; for save.
;;
;; ### Save
;; A save function needs to receive a "key" and a vector of path-html pairs, and
;; store each piece correctly in the given backend _such that it can easily be
;; looked up by the same key_. How should it store? However it likes. Whatever
;; makes it easiest to implement the load function.
;;
;; ### Load
;; The load function should be able to take a "key", and return a list of all
;; the paths for that key, plus a function that will load the data at the end of
;; each path. e.g.:
;; [["/"    root-fn]
;;  ["/foo" foo-fn]]

(defrecord BackendImplementation [name load-fn save-fn remove-fn])

(def temp-fs-backend (BackendImplementation. "temp-fs"
                                             temp-fs-impl/load-fn
                                             temp-fs-impl/save-fn
                                             temp-fs-impl/remove-fn))

(def permanent-fs-backend (BackendImplementation. "permanent-fs"
                                                  perm-fs-impl/load-fn
                                                  perm-fs-impl/save-fn
                                                  perm-fs-impl/remove-fn))
