(ns large-example.router
  #?(:cljs (:require-macros
             [large-example.macros :refer [defmutation defrouter]]))
  (:require
    #?(:clj [large-example.macros :refer [defmutation defrouter]])
            [devcards.core :as dc :refer-macros [defcard]]
            [om.next :as om :refer [defui]]
            [untangled.client.cards :refer-macros [untangled-app]]
            [untangled.client.mutations :as m]
            [untangled.client.logging :as log]
            [untangled.i18n :refer-macros [tr trc]]
            [om.dom :as dom]
            [untangled.client.core :as uc]))

(defui Screen1
  static uc/InitialAppState
  (initial-state [cls params] {:page :screen-1})
  static om/IQuery
  (query [this] [:page])
  Object
  (render [this]
    (dom/div nil "SCREEN 1")))

(defrouter MyRouter (fn [this props]
                      [(:page props) :report]) Screen1)

(def ui-route (om/factory MyRouter))

#?(:cljs
   (defcard sample
     "A sample router:"
     (ui-route {:page :screen-1})))
