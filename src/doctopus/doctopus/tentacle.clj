(ns doctopus.doctopus.tentacle
  "A `Tentacle' defines a single unit of documentation -- one 'source' worth,
  and all the configs required to build it."
  (:require [clojure.string :as str]
            [doctopus.shell :refer [make-html git-clone]]
            [doctopus.storage :refer [save-to-storage load-from-storage backend]]
            [me.raynes.fs :as fs]
            [taoensso.timbre :as log]))

(defn- get-source
  "We may eventually support more ways of getting source! Would be nice. Not
  today."
  [sc-location dest]
  (git-clone sc-location dest))

;; TENTACLES
(defprotocol TentacleMethods
  (load-html [this] "Makes sure the HTML has been generated for this tentacle")
  (generate-html [this])
  (save-build-output [this dir])
  (get-html-entrypoint [this])
  (routes [this]))

;; #### "Try, report, return"-ing
;; I've found myself doing a lot of this:
;; (if (do-a-thing-that-returns-something-or-nil)
;;     (do (log the result) true)
;;     (do (log the failure) nil))

(defn report-success
  [msg]
  (log/info msg)
  true)

(defn report-error
  [msg]
  (log/error msg)
  nil)

(defn check-and-report
  [result noun success-msg fail-msg]
  (let [success-tpl "Success: %s for %s"
        error-tpl   "Couldn't %s for %s"]
    (if result
      (report-success (format success-tpl noun success-msg))
      (report-error (format error-tpl noun fail-msg)))))

(defrecord Tentacle
    [name html-command html-args output-root source-location entry-point]
  TentacleMethods
  (load-html [this]
    (if (nil? (load-from-storage backend (:name this)))
      (generate-html this)
      (log/info "Found html for" (:name this))))
  (generate-html [this]
    (log/info "Generating HTML for" (:name this))
    (let [{:keys [html-command html-args source-location output-root]} this
          target-dir (fs/temp-dir "doctopus-clone")
          success? (get-source source-location (.getPath target-dir))]
      (if success?
        (do (binding [fs/*cwd* target-dir]
              (let [html-dir (fs/file output-root)]
                (apply make-html [html-command html-args (.getPath target-dir)])
                (save-build-output this html-dir)
                (log/info "Generated HTML for" name))))
        (log/error "Couldn't clone source! Argggggg!"))))
  (save-build-output [this dir]
    (let [{:keys [name]} this])
    (save-to-storage backend name dir))
  (get-html-entrypoint [this]
    (str/join "/" ["" "docs" (:name this) (:entry-point this)]))
  (routes [this]
    (load-from-storage backend (:name this))))
