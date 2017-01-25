(ns large-example.router
  (:require-macros
    [large-example.macros :refer [defmutation defrouter]])
  (:require
    [devcards.core :as dc :refer-macros [defcard]]
    [om.next :as om :refer [defui]]
    [untangled.client.cards :refer-macros [untangled-app]]
    [untangled.client.mutations :as m]
    [untangled.client.logging :as log]
    [untangled.i18n :refer-macros [tr trc]]
    [om.dom :as dom]
    [untangled.client.core :as uc]))

(defui SubScreen1
  static uc/InitialAppState
  (initial-state [cls params] {:page :screen-1})
  static om/IQuery
  (query [this] [:page])
  static om/Ident
  (ident [this props] [(:page props) :subscreen])
  Object
  (render [this]
    (dom/div nil "SUB SCREEN 1")))

(defui SubScreen2
  static uc/InitialAppState
  (initial-state [cls params] {:page :screen-2})
  static om/IQuery
  (query [this] [:page])
  static om/Ident
  (ident [this props] [(:page props) :subscreen])
  Object
  (render [this]
    (dom/div nil "SUB SCREEN 2")))

(defrouter SubRouter :subscreens (ident [this props] [(:page props) :subscreen]) :screen-1 SubScreen1 :screen-2 SubScreen2)

(def ui-subrouter (om/factory SubRouter))

(defui Screen1
  static uc/InitialAppState
  (initial-state [cls params] {:page :screen-1 :router (uc/get-initial-state SubRouter {})})
  static om/IQuery
  (query [this] [:page {:router (om/get-query SubRouter)}])
  static om/Ident
  (ident [this props] [(:page props) :report])
  Object
  (render [this]
    (let [{:keys [router]} (om/props this)]
      (dom/div nil "SCREEN 1"
        (dom/button #js {:onClick #(om/transact! this `[(set-route ~{:router :subscreens :new-route [:screen-1 :subscreen]})])} "sub 1")
        (dom/button #js {:onClick #(om/transact! this `[(set-route ~{:router :subscreens :new-route [:screen-2 :subscreen]})])} "sub 2")
        (ui-subrouter router)))))

(defui Screen2
  static uc/InitialAppState
  (initial-state [cls params] {:page :screen-2})
  static om/IQuery
  (query [this] [:page])
  static om/Ident
  (ident [this props] [(:page props) :report])
  Object
  (render [this]
    (dom/div nil "SCREEN 2")))

(defrouter MyRouter :mine! (ident [this props]
                                  [(:page props) :report]) :screen-1 Screen1 :screen-2 Screen2)

(def ui-route (om/factory MyRouter))

(defmutation set-route [router new-route]
  "Change routes on the given router to the designated new route id"
  (swap! state assoc-in [:routers/by-id router :current-route] new-route))

(defui App
  static uc/InitialAppState
  (initial-state [cls params] {:router (uc/get-initial-state MyRouter {})})
  static om/IQuery
  (query [this] [{:router (om/get-query MyRouter)}])
  Object
  (render [this]
    (let [{:keys [router]} (om/props this)]
      (dom/div nil
        (dom/button #js {:onClick #(om/transact! this `[(set-route ~{:router :mine! :new-route [:screen-1 :report]})])} "screen 1")
        (dom/button #js {:onClick #(om/transact! this `[(set-route ~{:router :mine! :new-route [:screen-2 :report]})])} "screen 2")
        (ui-route router)))))

(defcard sample
  "A sample router:"
  (untangled-app App)
  {}
  {:inspect-data true})
