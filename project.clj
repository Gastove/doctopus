(defproject doctopus "0.1.0-SNAPSHOT"
  :description "An un-opinionated framework for docs on the wobs"
  :url "http://github.com/Gastove/doctopus"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-beta3"]
                 [org.clojure/tools.reader "0.9.2"]
                 [org.clojure/data.json "0.2.6"]
                 [joda-time/joda-time "2.6"]
                 [markdown-clj "0.9.63"]
                 [me.raynes/fs "1.4.6"]
                 [http-kit "2.1.19"]
                 [jarohen/nomad "0.7.0"]
                 [com.taoensso/timbre "3.4.0"]
                 [ring/ring-defaults "0.1.4"]
                 [ring/ring-core "1.3.2"]
                 [ring/ring-devel "1.3.2"]
                 [bidi "1.19.0"]
                 [enlive "1.1.5"]
                 [ring/ring-mock "0.2.0"]
                 [korma "0.4.0"]
                 [clj-time "0.9.0"]
                 [postgresql/postgresql "8.4-702.jdbc4"]
                 [camel-snake-kebab "0.3.1" :exclusions [org.clojure/clojure]]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/clojurescript "0.0-3269"]
                 [cljs-http "0.1.30"]
                 [reagent "0.5.0"]]
  :plugins [[lein-marginalia "0.8.0"] [lein-cljsbuild "1.0.6"]]
  :main ^:skip-aot doctopus.web
  :target-path "target/%s"
  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :incremental false
                        :compiler {:main doctopus.main
                                   :source-map "resources/public/assets/scripts/main.js.map"
                                   :output-to "resources/public/assets/scripts/main.js"
                                   :output-dir "resources/public/assets/scripts"
                                   :asset-path "/assets/scripts"
                                   :optimizations :none
                                   :pretty-print true}}]}
  :profiles {:uberjar {:aot :all}})
