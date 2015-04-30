(ns doctopus.db.schema
  (:require [clojure.java.jdbc :as sql]
            [taoensso.timbre :as log]
            [doctopus.configuration :refer [server-config]]))

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
   [:head_name "varchar(50)"]
   [:output_root "varchar(50)"]
   [:html_commands "varchar(250)"]
   [:source_control "varchar(50)"]
   [:source_location "varchar(250)"]
   [:entry_point "varchar(50)"]
   [:created :timestamp "NOT NULL DEFAULT NOW()"]
   [:updated :timestamp "NOT NULL"]])

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
  (do
   (when (not (table-created? "heads"))
     (create-table! "heads" head-schema))
   (when (not (table-created? "tentacles"))
     (create-table! "tentacles" tentacle-schema))))
