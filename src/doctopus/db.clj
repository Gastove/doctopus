(ns doctopus.db
  (:require [korma.db :refer [defdb postgres]]
            [clojure.string :as string :refer [split-lines]]
            [korma.core :refer :all]
            [clj-time.coerce :refer [to-sql-time]]
            [clj-time.core :as clj-time]
            [camel-snake-kebab.core :refer [->snake_case_keyword ->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [doctopus.configuration :refer [server-config]]))

(defn- now
  []
  "returns a string for 'now' in SQL Timestamp format"
  (to-sql-time (clj-time/now)))

(defn- add-date-fields
  "adds created and updated fields to a map with given time"
  ([fields] (add-date-fields fields (now)))
  ([fields now-string]
  (let [new-fields (assoc fields :updated now-string)]
    (if (:created new-fields) new-fields
      (assoc new-fields :created now-string)))))

(defn- ->kebab-keys
  "converts all keys in a map to kebab-case keywords"
  [fields]
  (transform-keys ->kebab-case-keyword fields))

(defn- ->snake-keys
  "converts all keys in a map to snake-case keywords"
  [fields]
  (transform-keys ->snake_case_keyword fields))

(defdb main (postgres
           (select-keys (:database (server-config))
                        [:db :user :password :host :port])))

(defentity tentacles
  (pk :name)
  (prepare add-date-fields)
  (prepare (fn [{html-commands :html-commands :as tentacle}]
            (if html-commands
              (assoc tentacle :html-commands (string/join "\n" html-commands))
              tentacle)))
  (prepare ->snake-keys)
  (transform ->kebab-keys)
  (transform (fn [{html-commands :html-commands :as tentacle}]
              (if html-commands
                (assoc tentacle :html-commands (split-lines html-commands))
                tentacle))))


(defentity heads
  (pk :name)
  (prepare add-date-fields)
  (prepare ->snake-keys)
  (transform ->kebab-keys)
  (has-many tentacles {:fk :head_name}))

(defn get-tentacle
  [name]
  (first (select tentacles
                 (where {:name name})
                 (limit 1))))

(defn get-all-tentacles
  []
  (select tentacles))

(defn save-tentacle!
  [tentacle]
  (insert tentacles
          (values tentacle)))

(defn update-tentacle!
  [tentacle]
  (update tentacles
          (set-fields tentacle)
          (where {:name (:name tentacle)})))

(defn delete-tentacle!
  [name]
  (delete tentacles
          (where {:name name})))

(defn get-head
  [name]
  (first (select heads
                 (where {:name name})
                 ;; (with tentacles)
                 (limit 1))))

(defn get-all-heads
  []
  (select heads
          ;; (with tentacles)
          ))

(defn save-head!
  [head]
  (insert heads
          (values head)))

(defn update-head!
  [head]
  (update heads
          (set-fields head)
          (where {:name (:name head)})))

(defn delete-head!
  [name]
  (delete heads
          (where {:name name})))