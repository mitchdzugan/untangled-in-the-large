(ns large-example.main
  (:require [large-example.core :refer [app]]
            [untangled.client.core :as core]
            [large-example.ui.routing :as r]))

(reset! app (core/mount @app r/Root "app"))
