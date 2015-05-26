(ns doctopus.db
  (:require [camel-snake-kebab.core :refer [->snake_case_keyword ->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [clj-time.coerce :refer [to-sql-time]]
            [clj-time.core :as clj-time]
            [clojure.string :as str :refer [split-lines]]
            [doctopus.configuration :refer [server-config]]
            [korma.core :refer :all]
            [korma.db :refer [defdb postgres]]
            [taoensso.timbre :as log]))

(defn- now
  []
  "returns a string for 'now' in SQL Timestamp format"
  (to-sql-time (clj-time/now)))

(defn- add-updated
  "adds updated field to a map with given time"
  ([fields] (add-updated fields (now)))
  ([fields now-string] (assoc fields :updated now-string)))

(defn- ->kebab-keys
  "converts all keys in a map to kebab-case keywords"
  [fields]
  (transform-keys ->kebab-case-keyword fields))

(defn- ->snake-keys
  "converts all keys in a map to snake-case keywords"
  [fields]
  (transform-keys ->snake_case_keyword fields))

(defdb main (postgres
             (select-keys (get-in (server-config) [:database :main])
                          [:db :user :password :host :port])))

(declare head-tentacle-mappings documents)
(defentity tentacles
  (pk :name)
  (has-many head-tentacle-mappings {:fk :tentacle_name})
  (has-one documents {:fk :tentacle_name})
  (prepare add-updated)
  (prepare (fn [{html-commands :html-commands :as tentacle}]
             (if html-commands
               (assoc tentacle :html-commands (str/join "\n" html-commands))
               tentacle)))
  (prepare ->snake-keys)
  (transform ->kebab-keys)
  (transform (fn [{html-commands :html-commands :as tentacle}]
               (if html-commands
                 (assoc tentacle :html-commands (split-lines html-commands))
                 tentacle))))


(defentity heads
  (pk :name)
  (prepare add-updated)
  (prepare ->snake-keys)
  (transform ->kebab-keys)
  (has-many head-tentacle-mappings {:fk :head_name}))

;; ### Head <-> Tentacle Join Table
;; Heads and Tentacles can share the notorious SQL many-to-many
;; relationship. Further, having either a head or a tentacle record _on the
;; database_ storing information about the other gets conceptually weird. Korma
;; will automatically handle this by using two queries, but I'm unconvinced by
;; that as a data modeling approach. Enter: the join table.
;;
;; Join tables capture many-to-many relationships by inserting themselves in the
;; middle, effectively creating a one-to-many relationship between itself and
;; both of the other tables you're attempting to look up. Getting from Heads to
;; Tentacles (or vice-versa) is now a set of left-joins. Easy peasy!
(defentity head-tentacle-mappings
  (table :head_tentacle_mappings)
  (prepare ->snake-keys)
  (transform ->kebab-keys)
  (belongs-to heads)
  (belongs-to tentacles))

(defentity documents
  (table :documents)
  (prepare ->snake-keys)
  (prepare add-updated)
  (transform ->kebab-keys)
  (belongs-to tentacles)
  (entity-fields :name :uri :tentacle_name :body))


(defn get-tentacle
  [name]
  (first (select tentacles
                 (where {:name name})
                 (limit 1))))

(defn get-all-tentacles
  []
  (select tentacles))

(defn get-tentacles-for-head
  [head]
  (select tentacles
          (with head-tentacle-mappings
                (where {:head_name (:name head)}))))

(defn create-tentacle!
  [tentacle]
  (log/info "Creating new tentacle in the db:" tentacle)
  (insert tentacles
          (values tentacle)))

(defn update-tentacle!
  [tentacle]
  (log/info "Record found, updating tentacle:" (:name tentacle) "with values:" tentacle)
  (update tentacles
          (set-fields tentacle)
          (where {:name (:name tentacle)})))

(defn save-tentacle!
  [tentacle]
  (log/info "Saving tentacle" tentacle)
  (if-not (empty? (get-tentacle (:name tentacle)))
    (update-tentacle! tentacle)
    (create-tentacle! tentacle)))

(defn delete-tentacle!
  [tentacle]
  (log/warn "Deleting tentacle:" tentacle "!")
  (delete tentacles
          (where {:name (:name tentacle)})))

(defn get-head
  [name]
  (first (select heads
                 (where {:name name})
                 (limit 1))))

(defn get-all-heads
  []
  (select heads))

(defn get-heads-for-tentacle
  [tentacle]
  (select heads
          (with head-tentacle-mappings
                (where {:tentacle_name (:name tentacle)}))))

(defn create-head!
  [head]
  (log/info "Saving head:" head)
  (insert heads
          (values head)))

(defn update-head!
  [head]
  (log/info "Updating head:" (:name head) "with values" head)
  (update heads
          (set-fields head)
          (where {:name (:name head)})))

(defn save-head!
  [head]
  (if-not (empty? (get-head (:name head)))
    (update-head! head)
    (create-head! head)))

(defn delete-head!
  [head]
  (log/warn "Deleting head" head "!")
  (delete heads
          (where {:name (:name head)})))

(defn create-mapping!
  [head tentacle]
  (let [head-name (:name head)
        tentacle-name (:name tentacle)
        existing-mapping (select head-tentacle-mappings
                                 (where {:head_name head-name
                                         :tentacle_name tentacle-name}))]
    (if (empty? existing-mapping)
      (do
        (log/info "Creating mapping from head" head-name "to tentacle" tentacle-name)
        (insert head-tentacle-mappings
                (values {:head-name head-name :tentacle-name tentacle-name})))
      (log/info "Mapping from" head-name "to" tentacle-name "already exists"))))

(defn remove-mapping!
  [head tentacle]
  (log/warn "Removing mapping between head" (:name head) "and tentacle" (:name tentacle))
  (delete head-tentacle-mappings
          (where {:head-name (:name head)
                  :tentacle-name (:name tentacle)})))

(defn get-all-mappings
  []
  (select head-tentacle-mappings))

(defn get-mappings-for-head
  [head]
  (select head-tentacle-mappings
          (where {:name (:name head)})))

(defn get-mappings-for-tentacle
  [tentacle]
  (select head-tentacle-mappings
          (where {:name (:name tentacle)})))

(defn get-document-by-name
  [doc-name]
  (select documents
          (where {:name doc-name})))

(defn get-document-by-uri
  [uri]
  (select documents
          (where {:uri uri})))

(defn get-document-for-tentacle
  [tentacle]
  (select documents
          (where {:tentacle_name (:name tentacle)})))

(defn create-document!
  [document]
  (log/info "Creating new document named:" (:name document))
  (insert documents
          (values document)))

;; Manually
;; (let [sql (str/join ["UPDATE documents"
;;                      "SET search_vector = to_tsvector('english', body)"
;;                      "WHERE name = ?"] " ")]
;;   (exec ))
(defn update-document-index
  "The documents table has a column of type tsvector, `search_vector', which is
  indexed for Full Text Search; this function updates that column, for one
  document (in the case that a single doc has been modified) or for all
  documents (for use after loading in a large batch of new docs, for instance)."
  ([]
   (update documents
           (set-fields {:search-vector (raw "to_tsvector('english', body)")})
           (where {:uri [like "%html"]})))
  ([document]
   (update documents
           (set-fields {:search-vector (raw "to_tsvector('english', body)")})
           (where {:name (:name document)}))))

(defn update-document!
  [document update-index?]
  (log/info "Updating document named:" (:name document))
  (let [res (update documents
                    (set-fields document)
                    (where {:name (:name document)}))]
    (if update-index? (update-document-index))
    res))

(defn save-document!
  ([document] (save-document! document true))
  ([document update-index?]
   (if-not (empty? (get-document-by-name (:name document)))
     (update-document! document update-index?)
     (create-document! document))))
