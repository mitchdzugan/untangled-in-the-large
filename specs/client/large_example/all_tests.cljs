(ns large-example.all-tests
  (:require
    large-example.tests-to-run
    [doo.runner :refer-macros [doo-all-tests]]))

(doo-all-tests #".*-spec")
