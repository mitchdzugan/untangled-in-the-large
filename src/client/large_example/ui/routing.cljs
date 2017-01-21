(ns large-example.ui.routing
  (:require [om.next :as om]
            [bidi.verbose :refer [branch leaf param]]
            [untangled.client.mutations :as m]
            [pushy.core :as pushy]
            [bidi.bidi :as bidi]))

(def app-routes
  (branch
    "/"
    (leaf "" :main)
    (leaf "login" :login)
    (leaf "signup" :new-user)
    (leaf "reports" :reports-home)
    (branch "reports/" (param :report-id) (leaf "" :report))))

(defn update-route
  "Change the application's UI route to the given route."
  [{:keys [route]}] (comment "placeholder for IDE assistance"))

(defmethod m/mutate 'large-example.ui.routing/update-route [e k p]
  {:action #(js/console.log :update-route p)})

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
