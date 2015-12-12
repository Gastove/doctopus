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
  :plugins [[michaelblume/lein-marginalia "0.9.0" :exclusions [org.clojure/clojurescript]]]
  :main ^:skip-aot doctopus.web
  :target-path "target/%s"
  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]
  :cljsbuild {:builds {:app {:source-paths ["src-cljs"]
                             :compiler     {:output-to     "resources/public/assets/scripts/main.js"
                                            :output-dir    "resources/public/assets/scripts/out"
                                            :asset-path    "/assets/scripts/out"
                                            :optimizations :none
                                            :pretty-print  true}}}}
  :profiles {:dev      {:repl-options {:init-ns doctopus.web}
                        :dependencies [[lein-figwheel "0.4.1"]
                                       [org.clojure/tools.nrepl "0.2.11"]]
                        :plugins      [[lein-figwheel "0.5.0-2" :exclusions [joda-time]]
                                       [lein-cljsbuild "1.1.1" :exclusions [org.clojure/clojurescript]]]
                        :env          {:dev true}
                        :cljsbuild    {:builds {:app {:source-paths ["env/dev/src-cljs"]
                                                      :figwheel     true
                                                      :compiler     {:main       "doctopus.dev.main"
                                                                     :source-map true}}}}}
             :uberjar  {:hooks       [leiningen.cljsbuild]
                        :env         {:production true}
                        :aot         :all
                        :omit-source true
                        :cljsbuild   {:jar    true
                                      :builds {:app
                                               {:source-paths ["env/prod/src-cljs"]
                                                :compiler
                                                              {:optimizations :advanced
                                                               :pretty-print  false}}}}}})
