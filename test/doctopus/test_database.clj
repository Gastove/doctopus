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


(defn database-fixture
  [f]
  (bootstrap :test)
  (f)
  (doseq [n (keys table-name->schema)] (truncate! n)))
