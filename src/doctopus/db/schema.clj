(ns doctopus.db.schema
  "Define and bootstrap the Doctopus DB Schema."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as sql]
            [clojure.string :as str]
            [doctopus.db :as db]
            [doctopus.doctopus.head :as h]
            [doctopus.doctopus.tentacle :as t]
            [taoensso.timbre :as log]
            [doctopus.db.jdbc :refer [build-db-spec-by-name]]))

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
   [:output_root "varchar(50)"]
   [:html_commands "varchar(250)"]
   [:source_control "varchar(50)"]
   [:source_location "varchar(250)"]
   [:entry_point "varchar(50)"]
   [:created :timestamp "NOT NULL DEFAULT NOW()"]
   [:updated :timestamp "NOT NULL"]])

(def head-tentacle-schema
  [[:head_name "varchar(50) references heads(name) on delete cascade"]
   [:tentacle_name "varchar(50) references tentacles(name) on delete cascade"]
   ["PRIMARY KEY(head_name, tentacle_name)"]])

(def document-schema
  [[:name "varchar(50)" "PRIMARY KEY"]
   [:uri "varchar(100) NOT NULL"]
   [:body :text "NOT NULL"]
   [:mime_type "varchar(127) NOT NULL"] ;; Max length according to RFC http://tools.ietf.org/html/rfc4288#section-4.2
   [:image "bytea"]
   [:search_vector "tsvector"]
   [:tentacle_name "varchar(50) references tentacles(name) on delete cascade"]
   [:created :timestamp "NOT NULL DEFAULT NOW()"]
   [:updated :timestamp "NOT NULL"]])

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

(def table-name-schema-pairs [["heads" head-schema]
                              ["tentacles" tentacle-schema]
                              ["head_tentacle_mappings" head-tentacle-schema]
                              ["documents" document-schema]])

(defn- load-doctopus
  "On bootstrap, we want to create a Head, a Tentacle for Doctopus itself, and
  map the two together"
  []
  (let [head (h/->Head "main")
        tentacle-props-string (slurp (io/resource "self/heads/main/doctopus.edn"))
        tentacle-raw-props (edn/read-string tentacle-props-string)
        tentacle-parsed-props (h/parse-tentacle-config-map tentacle-raw-props)
        tentacle (t/map->Tentacle tentacle-parsed-props)]
    (db/save-head! head)
    (db/save-tentacle! tentacle)
    (db/create-mapping! head tentacle)))

(defn- create-fts-document-index
  [db-name]
  (let [update-sql "UPDATE documents SET search_vector = to_tsvector('english', name || ' ' || body)"
        idx-sql "CREATE INDEX fts_idx ON documents USING GIN(search_vector)"]
    (log/info "Creating FTS Index for the documents table")
    (do-sql-with-logging! update-sql db-name)
    (do-sql-with-logging! idx-sql db-name)
    (log/info "Index successfully created")))

(defn bootstrap
  "checks for the presence of tables and creates them if necessary"
  ([] (bootstrap :main))
  ([db-name]
   (doseq [[table-name schema] table-name-schema-pairs]
     (when (not (table-created? db-name table-name))
       (create-table! db-name table-name schema)))
   (load-doctopus)
   (create-fts-document-index db-name)))
