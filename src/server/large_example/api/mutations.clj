(ns large-example.api.mutations
  (:require

    [om.next.server :as oms]
    [taoensso.timbre :as timbre]
    [untangled.server.core :as core]))

(defmulti apimutate oms/dispatch)


