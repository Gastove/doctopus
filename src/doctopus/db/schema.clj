(ns doctopus.db.schema
  (:require [clojure.java.jdbc :as sql]
            [doctopus.configuration :refer [server-config]]))

(defn- get-subname
  []
  (let [{hostname :hostname port :port db :db} (:database (server-config))]
    (str "//" (or hostname "localhost") ":" (or port "5432") "/"
         (or db "doctopus"))))

(def db-spec {:classname "org.postgresql.Driver"
              :subprotocol "postgresql"
              :subname (get-subname)
              :user (:user (:database (server-config)))
              :password (:password (:database (server-config)))})

(defn table-created?
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
   [:head_name "varchar(50)"]
   [:output_root "varchar(50)"]
   [:html_commands "varchar(250)"]
   [:source_control "varchar(50)"]
   [:source_location "varchar(250)"]
   [:entry_point "varchar(50)"]
   [:created "varchar(50)" "NOT NULL"]
   [:updated "varchar(50)" "NOT NULL"]])

(defn- create-table!
  [table-name table-schema]
  (sql/db-do-commands db-spec
                      (apply sql/create-table-ddl
                       (cons (keyword table-name) table-schema))))

(defn bootstrap
  []
  (do
   (when (not (table-created? "heads")) (create-table! "heads" head-schema))
   (when (not (table-created? "tentacles"))
     (create-table! "tentacles" tentacle-schema))))
