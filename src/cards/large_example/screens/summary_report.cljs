(ns large-example.screens.summary-report
  (:require-macros
    [large-example.macros :refer [defmutation defroot]])
  (:require
    [large-example.ui.routing :as routing]
    [devcards.core :as dc :refer-macros [defcard]]
    [om.next :as om :refer [defui]]
    [untangled.client.cards :refer-macros [untangled-app]]
    [untangled.client.mutations :as m]
    [untangled.client.logging :as log]
    [untangled.i18n :refer-macros [tr trc]]
    [large-example.ui.screens.reports :as rpt]
    [om.dom :as dom]
    [untangled.client.core :as uc]))

(defonce summary-reconciler (atom nil))

(defroot Root rpt/SummaryReport)

(defcard summary-report
  (untangled-app routing/Root
    :started-callback (fn [{:keys [reconciler]}]
                        (reset! @routing/use-html5-routing false)
                        (reset! summary-reconciler reconciler)))
  {}
  {:inspect-data true})
