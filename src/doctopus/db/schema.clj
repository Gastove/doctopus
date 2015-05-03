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

(defn build-db-spec
  [{:keys [user password] :as cfg-map}]
  {:classname "org.postgresql.Driver"
   :subprotocol "postgresql"
   :subname (get-subname cfg-map)
   :user user
   :password password})

(defn build-db-spec-by-name
  [db-name]
  (let [db-map (get-in (server-config) [:database db-name])]
    (build-db-spec db-map)))

(defn table-created?
  "check our database for a table"
  [db-name table-name]
  (-> (sql/query (build-db-spec-by-name db-name)
                 [(str "select count(*) from information_schema.tables "
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
  [[:head_name "varchar(50) references heads(name) on delete cascade"]
   [:tentacle_name "varchar(50) references tentacles(name) on delete cascade"]])

(defn do-sql-with-logging!
  [sql-statement db-name]
  (try (sql/db-do-commands (build-db-spec-by-name db-name) sql-statement)
       (catch Exception e (log/error
                           (->> ["Database error! Attempting to run query:"
                                 sql-statement
                                 (str "Against database named: " db-name)
                                 "Hit exception:"
                                 (with-out-str (sql/print-sql-exception-chain e))]
                                (str/join \newline))))))

(defn- create-table!
  "creates a table with a given name and schema"
  [db-name table-name table-schema]
  (log/info "creating" table-name "table")
  (do-sql-with-logging! (apply sql/create-table-ddl
                               (cons (keyword table-name) table-schema))
                        db-name))

(def table-name->schema {"heads" head-schema
                         "tentacles" tentacle-schema
                         "head_tentacle_mappings" head-tentacle-schema})

(defn bootstrap
  "checks for the presence of tables and creates them if necessary"
  ([] (bootstrap :main))
  ([db-name]
   (doseq [[table-name schema] table-name->schema]
     (when (not (table-created? db-name table-name))
       (create-table! db-name table-name schema)))))
