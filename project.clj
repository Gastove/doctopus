(defproject doctopus "0.1.0-SNAPSHOT"
  :description "An un-opinionated framework for docs on the wobs"
  :url "http://github.com/Gastove/doctopus"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.reader "0.10.0"]
                 [cheshire "5.5.0"]
                 [joda-time/joda-time "2.6"]
                 [markdown-clj "0.9.63"]
                 [me.raynes/fs "1.4.6"]
                 [http-kit "2.1.19"]
                 [jarohen/nomad "0.7.0"]
                 [com.taoensso/timbre "3.4.0"]
                 [ring/ring-defaults "0.1.4"]
                 [ring/ring-core "1.3.2"]
                 [ring/ring-devel "1.3.2"]
                 [ring/ring-json "0.4.0"]
                 [bidi "1.19.0"]
                 [enlive "1.1.5"]
                 [ring/ring-mock "0.2.0"]
                 [korma "0.4.1"]
                 [clj-time "0.9.0"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/clojurescript "1.7.170"]
                 [cljs-http "0.1.38"]
                 [reagent "0.5.1"]
                 [org.postgresql/postgresql "9.4-1206-jdbc4"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [camel-snake-kebab "0.3.1" :exclusions [org.clojure/clojure]]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]]
  :plugins [[michaelblume/lein-marginalia "0.9.0" :exclusions [org.clojure/clojurescript]]
            [lein-figwheel "0.5.0-2" :exclusions [joda-time]]
            [lein-cljsbuild "1.1.1" :exclusions [org.clojure/clojurescript]]]
  :main ^:skip-aot doctopus.web
  :target-path "target/%s"
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src-cljs"]
                        :figwheel true
                        :incremental false
                        :compiler {:main doctopus.main
                                   :source-map "resources/public/assets/scripts/main.js.map"
                                   :output-to "resources/public/assets/scripts/main.js"
                                   :output-dir "resources/public/assets/scripts"
                                   :asset-path "/assets/scripts"
                                   :optimizations :none
                                   :pretty-print true}}
                       {:id "dev-omni"
                        :source-paths ["src-cljs"]
                        :figwheel true
                        :incremental false
                        :compiler {:main doctopus.omni
                                   :source-map "resources/public/assets/scripts/omni.js.map"
                                   :output-to "resources/public/assets/scripts/omni.js"
                                   :output-dir "resources/public/assets/scripts"
                                   :asset-path "/assets/scripts"
                                   :optimizations :none
                                   :pretty-print true}}]}
  :profiles {:uberjar {:aot :all
                       :prep-tasks ["compile" ["cljsbuild" "once" "prod"]]
                       :cljsbuild {:jar true
                                   :builds [{:id "prod"
                                             :source-paths ["src-cljs"]
                                             :figwheel false
                                             :compiler {:main doctopus.main
                                                        :source-map "resources/public/assets/scripts/main.js.map"
                                                        :output-to "resources/public/assets/scripts/main.js"
                                                        :asset-path "/assets/scripts"
                                                        :optimizations :advanced
                                                        :pretty-print false}}]}}})
