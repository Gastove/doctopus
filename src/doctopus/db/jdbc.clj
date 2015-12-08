(ns doctopus.db.jdbc
  "Interact directly with either the db driver or a db connection instance.

  It's a cool thing to do."
  (:require [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]
            [doctopus.configuration :refer [server-config]]))

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

(defn insert-image
  [file-name db-spec base-sql]
  (jdbc/with-db-connection [conn db-spec]
    (let [preped-statement (jdbc/prepare-statement conn base-sql)]
      (with-open [in-stream (io/input-stream file-name)]
        (.setBinaryStream preped-statement 1, in-stream, (long (.length in-stream)))
        (.execute preped-statement)))))
