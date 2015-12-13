(ns doctopus.test-database
  (:require [clojure.test :refer :all]
            [doctopus.configuration :refer [server-config]]
            [doctopus.db :refer :all]
            [doctopus
             [db :refer :all]]
            [doctopus.db.schema :refer :all]
            [korma.db :refer [default-connection postgres]]))

(defn truncate!
  [table-name]
  (let [sql-string (format "TRUNCATE %s CASCADE" table-name)]
    (do-sql-with-logging! sql-string :test)))

(defn obliterate!
  []
  (do-sql-with-logging! "DROP SCHEMA public CASCADE" :test)
  (do-sql-with-logging! "CREATE  SCHEMA public" :test))

(defn schema-only-fixture
  [f]
  (bootstrap-schema :test)
  (f)
  (obliterate!))

(defn schema-and-content-fixture
  [f]
  (bootstrap :test)
  (f)
  (obliterate!))
