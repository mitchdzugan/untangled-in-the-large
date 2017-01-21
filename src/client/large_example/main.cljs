(ns large-example.main
  (:require [large-example.core :refer [app]]
            [untangled.client.core :as core]
            [large-example.ui.root :as root]))

(reset! app (core/mount @app root/Root "app"))
