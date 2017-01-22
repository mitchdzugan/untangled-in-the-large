(ns large-example.ui.screens.reports
  (:require [om.next :as om]
            [untangled.client.mutations :as m]
            [om.dom :as dom]
            [untangled.client.core :as uc]))


(om/defui SummaryReport
          static uc/InitialAppState
          (initial-state [clz params] {:page :summary :label "SUMMARY"})
          static om/IQuery
          (query [this] [:page :label])
          Object
          (render [this]
                  (let [{:keys [label]} (om/props this)]
                    (dom/div nil
                             label))))

(def ui-summary-report (om/factory SummaryReport))

(om/defui DetailReport
          static uc/InitialAppState
          (initial-state [clz params] {:page :detail :label "DETAIL"})
          static om/IQuery
          (query [this] [:page :label])
          Object
          (render [this]
                  (let [{:keys [label]} (om/props this)]
                    (dom/div nil
                             label))))

(def ui-detail-report (om/factory DetailReport))

