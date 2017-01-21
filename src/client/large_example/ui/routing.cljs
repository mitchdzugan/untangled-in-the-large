(ns large-example.ui.routing
  (:require [om.next :as om]
            [bidi.verbose :refer [branch leaf param]]
            [untangled.client.mutations :as m]
            [om.dom :as dom]
            [pushy.core :as pushy]
            [bidi.bidi :as bidi]
            [untangled.client.core :as uc]))

(def app-routes
  (branch
    "/"
    (leaf "" :main)
    (leaf "login" :login)
    (leaf "signup" :new-user)
    (leaf "reports" :reports-home)
    (branch "reports/" (param :report-id) (leaf "" :report))))

(def suggested-app-state
  {:main          {:top {#_"App state for top screen"}}
   :login         {:top {#_"App state for login screen"}}
   :new-user      {:top {#_"App state for sign up screen"}}
   :reports       {:top {#_"App state for report routing, which can be empty"}}
   :summary       {:report {#_"App state for report summary screen"}}
   :detail        {:some-report-id {#_"App state for report detail screen"}}

   ; graph linkage of routers
   :top-screen    [:main :top]
   :report-screen [:summary :report]

   })

(def routing-tree
  {:main         {:top-screen [:main :top]}
   :login        {:top-screen [:login :top]}
   :new-user     {:top-screen [:new-user :top]}
   :reports-home {:top-screen    [:reports :top]
                  :report-screen [:summary :report]}
   :report       {:top-screen    [:reports :top]
                  :report-screen [:detail :param/report-id]}})

(defn update-routing-links
  "Given the app state map, returns a new map that has the routing graph links updated for the given route/params
  as a bidi match."
  [state-map {:keys [route]}]
  (let [{:keys [route-params handler]} route
        path-map (get routing-tree handler {})]
    (reduce (fn [m [k v]]
              (let [value (if (and (keyword? v) (= "param" (namespace v)))
                            (get route-params (keyword (name v)) v)
                            v)]
                (assoc m k value))) state-map path-map)))

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

(om/defui ReportsRouter
  static uc/InitialAppState
  (initial-state [clz params] (uc/initial-state SummaryReport {}))
  static om/Ident
  (ident [this props] [(:page props) :report])
  static om/IQuery
  (query [this] {:summary (om/get-query SummaryReport)
                 :detail  (om/get-query DetailReport)})
  Object
  (render [this]
    (let [{:keys [page] :as props} (om/props this)]
      (case page
        :summary (ui-summary-report props)
        :detail (ui-detail-report props)))))

(def ui-reports-router (om/factory ReportsRouter))

(om/defui Reports
  static uc/InitialAppState
  (initial-state [clz params] {:page :reports})
  static om/IQuery
  (query [this] [:page {[:report-screen '_] (om/get-query ReportsRouter)}])
  Object
  (render [this]
    (let [{:keys [report-screen]} (om/props this)]
      (dom/div nil
        "REPORTS SUBSCREEN"
        (ui-reports-router report-screen)))))

(def ui-reports (om/factory Reports))

(om/defui Main
  static uc/InitialAppState
  (initial-state [clz params] {:page :main :label "MAIN"})
  static om/IQuery
  (query [this] [:page :label])
  Object
  (render [this]
    (let [{:keys [label]} (om/props this)]
      (dom/div nil
        label))))

(def ui-main (om/factory Main))

(om/defui Login
  static uc/InitialAppState
  (initial-state [clz params] {:page :login :label "LOGIN"})
  static om/IQuery
  (query [this] [:page :label])
  Object
  (render [this]
    (let [{:keys [label]} (om/props this)]
      (dom/div nil
        label))))

(def ui-login (om/factory Login))

(om/defui NewUser
  static uc/InitialAppState
  (initial-state [clz params] {:page :new-user :label "New User"})
  static om/IQuery
  (query [this] [:page :label])
  Object
  (render [this]
    (let [{:keys [label]} (om/props this)]
      (dom/div nil
        label))))

(def ui-new-user (om/factory NewUser))

(om/defui TopRouter
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

(def ui-top-router (om/factory TopRouter))

(om/defui Root
  static uc/InitialAppState
  (initial-state [clz params] {:report-screen [:summary :report]
                               :top-screen    (uc/initial-state TopRouter {})})
  static om/IQuery
  (query [this] [{:top-screen (om/get-query TopRouter)}])
  Object
  (render [this]
    (dom/div nil
      "ROOT"
      (ui-top-router (:top-screen (om/props this))))))

(defn update-route
  "Change the application's UI route to the given route."
  [{:keys [route]}] (comment "placeholder for IDE assistance"))

(defmethod m/mutate 'large-example.ui.routing/update-route [{:keys [state]} k p]
  {:action (fn []
             (js/console.log :update-route p)
             (swap! state update-routing-links p))})

(defn- set-route! [reconciler match]
  (om/transact! reconciler `[(large-example.ui.routing/update-route ~{:route match})]))

(def history (atom nil))

(defn nav-to [env page]
  (pushy/set-token! @history (bidi/path-for app-routes page))
  (swap! (:state env) assoc :current-page [page :page]))

(defmethod m/mutate 'nav/new-user [env k p] {:action (fn [] (nav-to env :new-user))})
(defmethod m/mutate 'nav/login [env k p] {:action (fn [] (nav-to env :login))})
(defmethod m/mutate 'nav/main [env k p] {:action (fn [] (nav-to env :main))})

(comment
  (bidi/match-route app-routes "/reports/5")
  (bidi/path-for app-routes :report :report-id 6))
