(ns doctopus.web
  (:require [ring.middleware.defaults :refer :all]
            [ring.middleware.reload :as reload]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.stacktrace :as trace]))
