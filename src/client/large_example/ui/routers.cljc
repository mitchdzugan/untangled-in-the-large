(ns large-example.ui.routers
  #?(:cljs
     (:require-macros [large-example.macros :as m]))
  (:require #?(:clj [large-example.macros :as m])
                    om.next
                    untangled.client.core
                    [large-example.ui.screens.reports :as r]))

(m/defrouter ReportsRouter (fn [this props] [(:page props) :report]) r/SummaryReport r/DetailReport)

#_(def ui-reports-router (om/factory ReportsRouter))

#_(om/defui TopRouter
            static uc/InitialAppState
            (initial-state [clz params] (uc/initial-state Main {}))
            static om/Ident
            (ident [this props] [(:page props) :top])
            static om/IQuery
            (query [this] {:main     (om/get-query Main)
                           :login    (om/get-query Login)
                           :new-user (om/get-query NewUser)
                           :reports  (om/get-query Reports)})
            Object
            (render [this]
                    (let [{:keys [page] :as props} (om/props this)]
                      (dom/div nil
                               (js/console.log :page page)
                               (case page
                                 :main (ui-main props)
                                 :login (ui-login props)
                                 :new-user (ui-new-user props)
                                 :reports (ui-reports props))))))

#_(def ui-top-router (om/factory TopRouter))
