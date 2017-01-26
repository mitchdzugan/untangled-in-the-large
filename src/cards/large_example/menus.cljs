(ns large-example.menus
  (:require-macros
    [large-example.macros :refer [defmutation]])
  (:require [devcards.core :as dc :refer-macros [defcard]]
            [om.next :as om :refer-macros [defui]]
            [large-example.utils :as u]
            [large-example.ui.components.menus :as menu]
            [cljs.pprint :refer [pprint]]
            [untangled.client.cards :refer-macros [untangled-app]]
            [untangled.client.mutations :as m]
            [untangled.i18n :refer-macros [tr trc]]
            [om.dom :as dom]
            [untangled.client.core :as uc]))

(def a-closed-menu (menu/make-popup-menu :main-menu "Main" [(menu/make-menu-item :file "File") (menu/make-menu-item :open "Open")]))
(def an-open-menu (menu/set-popup a-closed-menu true))

(defcard popup-menu-card
  "A popup menu (closed)"
  (menu/ui-popup a-closed-menu))

(defcard open-popup-menu-card
  "A popup menu (open)"
  (menu/ui-popup an-open-menu))

;; A simple simulation of an application with two menus
(defui ActiveMenuRoot
  static uc/InitialAppState
  (initial-state [cls {:keys [id label items]}]
    {:menu  (menu/make-popup-menu :test-menu "Menu Label"
                                  [(menu/make-menu-item :open "Open") (menu/make-menu-item :close "Close") (menu/make-menu-item :copy "Copy")])
     :menu2 (menu/make-popup-menu :test-menu-2 "Other Menu"
                                  [(menu/make-menu-item :jump "Jump") (menu/make-menu-item :shout "Shout") (menu/make-menu-item :dance "Dance")])})
  static om/IQuery
  (query [this] [{:menu (om/get-query menu/PopupMenu)} {:menu2 (om/get-query menu/PopupMenu)}])
  Object
  (render [this]
    (let [{:keys [menu menu2]} (om/props this)]
      (dom/div #js {:className "bg-info" :onClick #(om/transact! this `[(menu/close-all-menus {})])}
        (menu/ui-popup (om/computed menu {:onSelect (fn [id] (js/alert (str "You selected " id)))}))
        (menu/ui-popup (om/computed menu2 {:onSelect (fn [id] (js/alert (str "You selected " id)))}))))))

(defonce the-app (atom nil))

(defn log-app-state [app] (-> app :reconciler om/app-state deref pprint))

(defcard active-popup-menu-card
  "An active popup in an Untangled Application, with state.

  The desired behavior is as follows:

  - Clicking on a closed menu opens it.
  - Clicking on an open menu closes it.
  - All open menus auto-close if another menu is selected.
  - All open menus auto-close if some other part (the blue area) of the application is clicked.
  - Selecting a menu item is detected (ID of item is sent to callback).
  - Selecting a menu item closes the menu.
  "
  (untangled-app ActiveMenuRoot
    :started-callback (fn [app] (reset! the-app app)))
  {}
  {:inspect-data true})                                    ; set to true to see app state
