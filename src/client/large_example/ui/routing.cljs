(ns large-example.ui.routing
  (:require-macros [large-example.macros :refer [defrouter defplaceholder defroot]])
  (:require [om.next :as om]
            [bidi.verbose :refer [branch leaf param]]
            [untangled.client.mutations :as m]
            [om.dom :as dom]
            [pushy.core :as pushy]
            [bidi.bidi :as bidi]
            [untangled.client.core :as uc]))

(declare nav-to!)

(om/defui ^:once Main
  static uc/InitialAppState
  (initial-state [clz params] {:page :main :label "MAIN"})
  static om/IQuery
  (query [this] [:page :label])
  Object
  (render [this]
    (let [{:keys [label]} (om/props this)]
      (dom/div nil
        (dom/a #js {:onClick #(nav-to! this :new-user)} "New User")
        (dom/a #js {:onClick #(nav-to! this :login)} "Login")
        (dom/a #js {:href "/status/a"} "Status A")
        (dom/a #js {:href "/graph/a"} "Graph A")
        label))))

(def ui-main (om/factory Main))

(om/defui ^:once Login
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

(om/defui ^:once NewUser
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

(defplaceholder StatusReport {:id :a :page :status-report}
  (render [this] (let [{:keys [id]} (om/props this)]
                   (dom/div nil (str "Status " id)))))

(defplaceholder GraphingReport {:id :a :page :graphing-report}
  (render [this] (let [{:keys [id]} (om/props this)]
                   (dom/div nil (str "Graph " id)))))

(defrouter ReportRouter :report-router
  (ident [this props]
         [(:page props) (:id props)])
  :status-report StatusReport
  :graphing-report GraphingReport
  )


#_(om/defui ^:once TopRouter-Union
    static uc/InitialAppState
    (initial-state [clz params] (uc/get-initial-state Main {}))
    static om/Ident
    (ident [this props] [(:page props) :top])
    static om/IQuery
    (query [this] {:main     (om/get-query Main)
                   :reports  (om/get-query ReportRouter)
                   :login    (om/get-query Login)
                   :new-user (om/get-query NewUser)})
    Object
    (render [this]
      (let [{:keys [page] :as props} (om/props this)]
        (dom/div nil
          (case page
            :main (ui-main props)
            :login (ui-login props)
            :new-user (ui-new-user props)
            (dom/div nil (str "BAD PAGE" page)))))))

#_(def ui-top-router (om/factory TopRouter-Union))

#_(om/defui ^:once TopRouter
    static uc/InitialAppState
    (initial-state [clz params] {:current-route (uc/get-initial-state TopRouter-Union {})})
    static om/Ident
    (ident [this props] [:routers/by-id :top-screen])
    static om/IQuery
    (query [this] [{:current-route (om/get-query TopRouter-Union)}])
    Object
    (render [this]
      (ui-top-router (:current-route (om/props this)))))

#_(def ui-top (om/factory TopRouter))

; BIG GOTCHA: Make sure you query for the prop (in this case :page) that the union needs in order to decide. It won't pull it itself!
(om/defui ^:once ReportsMain
  static uc/InitialAppState
  (initial-state [clz params] {:page :report :report-router (uc/get-initial-state ReportRouter {})})
  static om/IQuery
  (query [this] [:page {:report-router (om/get-query ReportRouter)}])
  Object
  (render [this]
    (dom/div nil
      "REPORT MAIN SCREEN"
      ((om/factory ReportRouter) (:report-router (om/props this))))))


(defrouter TopRouter :top-screen
  (ident [this props] [(:page props) :top])
  :main Main
  :login Login
  :new-user NewUser
  :report ReportsMain)

#_(om/defui ^:once Root
    static uc/InitialAppState
    (initial-state [clz params] {:top-screen (uc/get-initial-state TopRouter {})})
    static om/IQuery
    (query [this] [:ui/react-key {:top-screen (om/get-query TopRouter)}])
    Object
    (render [this]
      (let [{:keys [ui/react-key top-screen]} (om/props this)]
        (dom/div #js {:key react-key}
          (ui-top top-screen)))))

(defroot Root TopRouter)


(def routing-tree
  "A map of routes. The top key is the name of the route (returned from bidi). The value
  is a map. In this map:

  - The keys are the IDs of the routers that must be updated to show the route, and whose
  - The values are the target screen ident. A value in this ident using the `param` namespace will be
  replaced with the incoming route parameter."
  {:main     {:top-screen [:main :top]}
   :login    {:top-screen [:login :top]}
   :new-user {:top-screen [:new-user :top]}
   :graph    {:top-screen    [:report :top]
              :report-router [:graphing-report :param/report-id]}
   :status   {:top-screen    [:report :top]
              :report-router [:status-report :param/report-id]}})

(defn update-routing-links
  "Given the app state map, returns a new map that has the routing graph links updated for the given route/params
  as a bidi match."
  [state-map {:keys [route]}]
  (let [{:keys [route-params handler]} route
        path-map (get routing-tree handler {})]
    (reduce (fn [m [router-id ident-to-set]]
              (let [value (mapv (fn [element]
                                  (if (and (keyword? element) (= "param" (namespace element)))
                                    (keyword (get route-params (keyword (name element)) element))
                                    element))
                                ident-to-set)]
                (assoc-in m [:routers/by-id router-id :current-route] value))) state-map path-map)))

(defn update-route
  "Change the application's UI route to the given route."
  [{:keys [route]}] (comment "placeholder for IDE assistance"))

(defmethod m/mutate 'large-example.ui.routing/update-route [{:keys [state]} k p]
  {:action (fn [] (swap! state update-routing-links p))})

(defn set-route!
  "Change the route using a bidi match. This method can be directly hooked to pushy via bidi at startup:

  ```
  (pushy/pushy (partial r/set-route! reconciler) (partial bidi/match-route r/app-routes))
  ```

  You probably will want to save the pushy return value for programatic routing via `pushy/set-token!`
  "
  [comp-or-reconciler match]
  (om/transact! comp-or-reconciler `[(large-example.ui.routing/update-route ~{:route match})]))

(def history (atom nil))

(defonce use-html5-routing (atom true))

(def app-routes
  "The bidi routing map for the application. The leaf keywords are the route names. Parameters
  in the route are available for use in the routing algorithm as :param/param-name."
  (branch
    "/"
    (leaf "index-dev.html" :main)
    (leaf "index.html" :main)
    (leaf "" :main)
    (leaf "login" :login)
    (leaf "signup" :new-user)
    (branch "status/" (param :report-id) (leaf "" :status))
    (branch "graph/" (param :report-id) (leaf "" :graph))))


(defn nav-to!
  [component page & kvs]
  (if (and @history @use-html5-routing)
    (pushy/set-token! @history (bidi/path-for app-routes page))
    (om/transact! component `[(large-example.ui.routing/update-route ~{:handler page :route-params (into {} kvs)})])))

(defn nav-to [env page]
  (pushy/set-token! @history (bidi/path-for app-routes page))
  (swap! (:state env) assoc :current-page [page :page]))

