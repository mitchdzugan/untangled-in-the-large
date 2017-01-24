(ns large-example.ui.screens.reports
  (:require [om.next :as om]
            [untangled.client.mutations :as m]
            [om.dom :as dom]
    #?(:cljs [large-example.ui.charting :as chart])
            [untangled.client.core :as uc]))


(defmethod m/mutate 'large-example.ui.screens.reports/extend-width [{:keys [state]} k params]
  {:action (fn []
             (swap! state update-in [:summary :report :width] inc))})

(om/defui SummaryReport
  static uc/InitialAppState
  (initial-state [clz params] {:page        :summary :label "SUMMARY"
                               :width       1
                               :data-points [{:x 1 :y 2} {:x 2 :y 9} {:x 3 :y 23}
                                             {:x 4 :y 4} {:x 5 :y 7}]})
  static om/IQuery
  (query [this] [:page :label :data-points])
  Object
  (render [this]
    (let [{:keys [label data-points width]} (om/props this)]
      (dom/div nil
        (dom/button #js {:onClick #(om/transact! this `[(extend-width)])} "Stretch")
        #?(:cljs (chart/vcontainer #js {:width 400 :height 200 :responsive false}
                   (chart/vchart #js {}
                     (chart/vbar #js {:domainPadding width :data (clj->js data-points)}))))
        (str label)))))

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

