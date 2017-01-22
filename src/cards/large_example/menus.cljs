(ns large-example.menus
  (:require-macros
    [large-example.macros :refer [defmutation]])
  (:require [devcards.core :as dc :refer-macros [defcard]]
            [om.next :as om :refer-macros [defui]]
            [large-example.utils :as u]
            [large-example.ui.components.menus :as menu]
            [untangled.client.cards :refer-macros [untangled-app]]
            [untangled.client.mutations :as m]
            [untangled.i18n :refer-macros [tr trc]]
            [om.dom :as dom]
            [untangled.client.core :as uc]))

;; A simple simulation of an application with two menus
(defui ActiveMenuRoot
  static uc/InitialAppState
  (initial-state [cls {:keys [id label items]}]
    {:menu  (menu/popup-menu :test-menu "Menu Label"
                             [(menu/menu-item :open "Open") (menu/menu-item :close "Close") (menu/menu-item :copy "Copy")])
     :menu2 (menu/popup-menu :test-menu-2 "Other Menu"
                             [(menu/menu-item :jump "Jump") (menu/menu-item :shout "Shout") (menu/menu-item :dance "Dance")])})
  static om/IQuery
  (query [this] [{:menu (om/get-query menu/PopupMenu)} {:menu2 (om/get-query menu/PopupMenu)}])
  Object
  (render [this]
    (let [{:keys [menu menu2]} (om/props this)]
      (dom/div #js {:className "bg-info" :onClick #(om/transact! this `[(menu/close-all-menus {})])}
        (menu/ui-popup (om/computed menu {:onSelect (fn [id] (js/alert (str "You selected " id)))}))
        (menu/ui-popup (om/computed menu2 {:onSelect (fn [id] (js/alert (str "You selected " id)))}))))))

(defcard popup-menu-card
  "A popup menu (closed)"
  (menu/ui-popup {:menu/id :main-menu :menu/label "Main"}))

(defcard open-popup-menu-card
  "A popup menu (open)"
  (menu/ui-popup {:menu/id :main-menu :menu/open? true :menu/label "Main" :menu/items [{:item/id :file :item/label "File"}]}))

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
  (untangled-app ActiveMenuRoot)
  {}
  {:inspect-data true})                                    ; set to true to see app state
