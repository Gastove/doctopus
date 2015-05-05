(ns doctopus.test-database
  (:require [clojure.test :refer :all]
            [doctopus.configuration :refer [server-config]]
            [doctopus.db :refer :all]
            [doctopus.db.schema :refer :all]
            [korma.db :refer [defdb postgres]]
            [clojure.test :refer :all]
            [doctopus
             [db :refer :all]]
            [doctopus.db.schema :refer :all]
            [clojure.test :refer :all]
            [clojure.test :refer :all]
            [doctopus
             [db :refer :all]]
            [doctopus.db.schema :refer :all]))


(defdb test-db (postgres
                (select-keys (get-in (server-config) [:database :test])
                             [:db :user :password :host :port])))

(defn- truncate!
  [table-name]
  (let [sql-string (format "TRUNCATE %s CASCADE" table-name)]
    (do-sql-with-logging! sql-string :test)))

(defn obliterate!
  []
  (do-sql-with-logging! "DROP SCHEMA public CASCADE" :test)
  (do-sql-with-logging! "CREATE  SCHEMA public" :test))

(defn database-fixture
  [f]
  (bootstrap :test)
  (f)
  (obliterate!))
