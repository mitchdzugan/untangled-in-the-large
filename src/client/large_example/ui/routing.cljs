(ns large-example.ui.routing
  (:require [om.next :as om]
            [bidi.verbose :refer [branch leaf param]]
            [untangled.client.mutations :as m]
            [large-example.ui.screens.reports :refer [DetailReport SummaryReport ui-summary-report ui-detail-report]]
            [om.dom :as dom]
            [pushy.core :as pushy]
            [bidi.bidi :as bidi]
            [untangled.client.core :as uc]))

(def app-routes
  "The bidi routing map for the application. The leaf keywords are the route names. Parameters
  in the route are available for use in the routing algorithm as :param/param-name."
  (branch
    "/"
    (leaf "" :main)
    (leaf "login" :login)
    (leaf "signup" :new-user)
    (leaf "reports" :reports-home)
    (branch "reports/" (param :report-id) (leaf "" :report))))

(def routing-tree
  "A map of routes. The top key is the name of the route (returned from bidi). The value
  is a map. In this map:

  - The keys are the IDs of the routers that must be updated to show the route, and whose
  - The values are the target screen ident. A value in this ident using the `param` namespace will be
  replaced with the incoming route parameter."
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

(om/defui TopRouter-Union
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

(def ui-top-router (om/factory TopRouter-Union))

(om/defui TopRouter
  static uc/InitialAppState
  (initial-state [clz params] {:id 1 :current-route (uc/initial-state TopRouter-Union {})})
  static  om/Ident
  (ident [this props] [:routers/by-id (:id props)])
  static om/IQuery
  (query [this] [{:current-route (om/get-query TopRouter-Union)}])
  Object
  (render [this]
    (dom/div nil
      "TOP ROUTER"
      (ui-top-router (:current-route (om/props this))))))

(def ui-top (om/factory TopRouter))

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
      (ui-top (:top-screen (om/props this))))))


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
