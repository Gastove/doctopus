(ns doctopus.db.schema
  (:require [clojure.java.jdbc :as sql]
            [taoensso.timbre :as log]
            [doctopus.configuration :refer [server-config]]))

(defn- get-subname
  "pull database options out of the local config"
  []
  (let [{host :host port :port db :db} (:database (server-config))]
    (str "//" (or host "localhost") ":" (or port "5432") "/"
         (or db "doctopus"))))

(def db-spec {:classname "org.postgresql.Driver"
              :subprotocol "postgresql"
              :subname (get-subname)
              :user (:user (:database (server-config)))
              :password (:password (:database (server-config)))})

(defn table-created?
  "check our database for a table"
  [table-name]
  (-> (sql/query db-spec [(str "select count(*) from information_schema.tables "
                           "where table_name='" table-name "'")])
      first :count pos?))

(def head-schema
  [[:name :varchar "PRIMARY KEY"]
   [:created :timestamp "NOT NULL"]
   [:updated :timestamp "NOT NULL"]])

(def tentacle-schema
  [[:name "varchar(50)" "PRIMARY KEY"]
;   [:head_name "varchar(50)"]
   [:output_root "varchar(50)"]
   [:html_commands "varchar(250)"]
   [:source_control "varchar(50)"]
   [:source_location "varchar(250)"]
   [:entry_point "varchar(50)"]
   [:created "varchar(50)" "NOT NULL"]
   [:updated "varchar(50)" "NOT NULL"]])

(def head-tentacle-schema
  [[:head-name "varchar(50)" "FOREIGN KEY"]
   [:tentacle-name "varchar(50)" "FOREIGN KEY"]])

(defn- create-table!
  "creates a table with a given name and schema"
  [table-name table-schema]
  (log/info "creating" table-name "table")
  (sql/db-do-commands db-spec
                      (apply sql/create-table-ddl
                       (cons (keyword table-name) table-schema))))

(defn bootstrap
  "checks for the presence of tables and creates them if necessary"
  []
  (let [assure {"heads" head-schema
                "tentacles" tentacle-schema
                "head_tentacle_mappings" head-tentacle-schema}]
    (doseq [[table-name schema] assure]
      (when (not (table-created? table-name))
        (create-table! "heads" schema)))))
