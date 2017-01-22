(defproject untangled-large "0.1.0-SNAPSHOT"
  :description "An example of how to structure larger apps"
  :license {:name "MIT" :url "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/clojurescript "1.9.293"]
                 [bidi "2.0.16"]
                 [kibu/pushy "0.3.6"]
                 [commons-io "2.5"]

                 [hickory "0.7.0"]
                 [navis/untangled-client "0.6.1-SNAPSHOT"]
                 [untangled/om-css "1.0.0"]
                 [org.omcljs/om "1.0.0-alpha47"]

                 [navis/untangled-spec "0.3.10-SNAPSHOT" :scope "test" :exclusions [io.aviso/pretty]]
                 [lein-doo "0.1.7" :scope "test"]
                 [org.clojure/core.async "0.2.395"]
                 [http-kit "2.2.0"]
                 [com.taoensso/timbre "4.7.4"]
                 [navis/untangled-server "0.6.2"]]

  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-doo "0.1.7"]
            [com.jakemccrary/lein-test-refresh "0.18.0"]]

  :doo {:build "automated-tests"
        :paths {:karma "node_modules/karma/bin/karma"}}

  :uberjar-name "large-example.jar"

  :test-refresh {:report       untangled-spec.reporters.terminal/untangled-report
                 :with-repl    true
                 :changes-only true}

  :source-paths ["src/server"]
  :test-paths ["specs" "specs/server" "specs/config"]
  :clean-targets ^{:protect false} ["target" "resources/public/js" "resources/private"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :cljsbuild {:builds [{:id           "production"
                        :source-paths ["src/client"]
                        :jar          true
                        :compiler     {:asset-path    "js/prod"
                                       :main          large-example.main
                                       :optimizations :simple
                                       :output-dir    "resources/public/js/prod"
                                       :output-to     "resources/public/js/large-example.min.js"}}
                       {:id           "dev"
                        :figwheel     true
                        :source-paths ["src/client" "dev/client"]
                        :compiler     {:asset-path           "js/dev"
                                       :external-config
                                       {:devtools/config
                                        ;;github.com/binaryage/cljs-devtools/blob/master/docs/configuration.md
                                        {:print-config-overrides true}}
                                       :main                 cljs.user
                                       :optimizations        :none
                                       :output-dir           "resources/public/js/dev"
                                       :output-to            "resources/public/js/large-example.js"
                                       :preloads             [devtools.preload]
                                       :source-map-timestamp true}}
                       {:id           "test"
                        :source-paths ["specs/client" "src/client"]
                        :figwheel     true
                        :compiler     {:asset-path    "js/specs"
                                       :main          large-example.spec-main
                                       :optimizations :none
                                       :output-dir    "resources/public/js/specs"
                                       :output-to     "resources/public/js/specs.js"
                                       :preloads      [devtools.preload]}}
                       {:id           "automated-tests"
                        :source-paths ["specs/client" "src/client"]
                        :compiler     {:asset-path    "js/ci"
                                       :main          large-example.all-tests
                                       :optimizations :none
                                       :output-dir    "resources/private/js/ci"
                                       :output-to     "resources/private/js/unit-tests.js"}}
                       {:id           "cards"
                        :figwheel     {:devcards true}
                        :source-paths ["src/client" "src/cards"]
                        :compiler     {:asset-path           "js/cards"
                                       :main                 large-example.cards
                                       :optimizations        :none
                                       :output-dir           "resources/public/js/cards"
                                       :output-to            "resources/public/js/cards.js"
                                       :preloads             [devtools.preload]
                                       :source-map-timestamp true}}]}

  :profiles {:uberjar {:main       large-example.core
                       :aot        :all
                       :prep-tasks ["compile"
                                    ["cljsbuild" "once" "production"]]}
             :dev     {:source-paths ["dev/client" "dev/server" "src/client" "src/server"]
                       :dependencies [[binaryage/devtools "0.8.3"]
                                      [org.clojure/tools.namespace "0.2.11"]
                                      [com.cemerick/piggieback "0.2.1"]
                                      [figwheel-sidecar "0.5.8" :exclusions [org.clojure/tools.reader]]
                                      [devcards "0.2.2" :exclusions [org.omcljs/om]]]
                       :repl-options {:init-ns          user
                                      :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
