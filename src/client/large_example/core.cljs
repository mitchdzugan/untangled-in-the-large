(ns large-example.core
  (:require [om.next :as om]
            [untangled.client.core :as uc]
            [untangled.client.data-fetch :as f]
            [untangled.client.mutations :as m]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            large-example.state.mutations
            [large-example.ui.routing :as r]
            [untangled.client.logging :as log]))

(defn merge-mutations [state k p]
  (log/info "Got return value for " k " -> " p)
  state)

(defonce app
         (atom (uc/new-untangled-client
                 :mutation-merge merge-mutations
                 :started-callback (fn [{:keys [reconciler]}]
                                     (reset! r/history (pushy/pushy (partial r/set-route! reconciler) (partial bidi/match-route r/app-routes)))
                                     (pushy/start! @r/history)
                                     (pushy/set-token! @r/history "/login")
                                     (pushy/set-token! @r/history "/")
                                     (pushy/set-token! @r/history "/reports")
                                     ;(f/load-data reconciler [:logged-in? :current-user] :post-mutation 'login/login-complete)
                                     ;;TODO: initial load of data
                                     ))))
