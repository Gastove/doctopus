(ns doctopus.db.schema
  (:require [clojure.java.jdbc :as sql]
            [clojure.string :as str]
            [doctopus.configuration :refer [server-config]]
            [taoensso.timbre :as log]))

(defn- get-subname
  "pull database options out of the local config"
  [{:keys [host port db] :or {host "localhost" port 5432 db "doctopus"}}]
  (let [tpl "//%s:%d/%s"]
    (format tpl host port db)))

(def db-spec {:classname "org.postgresql.Driver"
              :subprotocol "postgresql"
              :subname (get-subname (:database (server-config)))
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
   [:created :timestamp "NOT NULL DEFAULT NOW()"]
   [:updated :timestamp "NOT NULL"]])

(def tentacle-schema
  [[:name "varchar(50)" "PRIMARY KEY"]
;   [:head_name "varchar(50)"]
   [:output_root "varchar(50)"]
   [:html_commands "varchar(250)"]
   [:source_control "varchar(50)"]
   [:source_location "varchar(250)"]
   [:entry_point "varchar(50)"]
   [:created :timestamp "NOT NULL DEFAULT NOW()"]
   [:updated :timestamp "NOT NULL"]])

(def head-tentacle-schema
  [[:head_name "varchar(50) references heads(name)"]
   [:tentacle_name "varchar(50) references tentacles(name)"]])

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
