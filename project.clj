(defproject doctopus "0.1.0-SNAPSHOT"
  :description "An un-opinionated framework for docs on the wobs"
  :url "http://github.com/Gastove/doctopus"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [joda-time/joda-time "2.6"]
                 [markdown-clj "0.9.63"]
                 [me.raynes/fs "1.4.6"]
                 [http-kit "2.1.19"]
                 [jarohen/nomad "0.7.0"]
                 [com.taoensso/timbre "3.4.0"]
                 [ring/ring-defaults "0.1.4"]
                 [ring/ring-core "1.3.2"]
                 [ring/ring-devel "1.3.2"]
                 [bidi "1.18.10"]
                 [enlive "1.1.5"]
                 [ring/ring-mock "0.2.0"]
                 [korma "0.4.0"]
                 [org.xerial/sqlite-jdbc "3.7.15-M1"]
                 [clj-time "0.9.0"]
                 [camel-snake-kebab "0.3.1" :exclusions [org.clojure/clojure]]]
  :plugins [[lein-marginalia "0.8.0"]]
  :main ^:skip-aot doctopus.web
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
