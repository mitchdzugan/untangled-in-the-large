(ns large-example.spec-main
  (:require-macros
    [untangled-spec.reporters.suite :as ts])
  (:require
    large-example.tests-to-run))

(enable-console-print!)

(ts/deftest-all-suite specs #".*-spec")

(specs)
