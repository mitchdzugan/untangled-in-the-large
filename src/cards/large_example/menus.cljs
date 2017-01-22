(ns large-example.menus
  (:require [devcards.core :as dc :refer-macros [defcard]]
            [om.next :as om :refer-macros [defui]]
            [large-example.utils :as u]
            [untangled.client.cards :refer-macros [untangled-app]]
            [untangled.client.mutations :as m]
            [untangled.i18n :refer-macros [tr trc]]
            [om.dom :as dom]
            [untangled.client.core :as uc]))


(defui MenuItem
  static uc/InitialAppState
  (initial-state [cls {:keys [id label]}] {:menu-item/id id :menu-item/label label})
  static om/Ident
  (ident [this props] [:menu-item/by-id (:menu-item/id props)])
  static om/IQuery
  (query [this] [:menu-item/id :menu-item/label])
  Object
  (render [this]
    (let [{:keys [menu-item/id menu-item/label]} (om/props this)
          onSelect (or (om/get-computed this :onSelect) identity)
          clickHandler (fn [evt]
                         (.stopPropagation evt)
                         (om/transact! this '[(menu/close-all-menus) :menu/id])
                         (onSelect id))]
      (dom/li nil
              (dom/a #js {:onClick clickHandler} label)))))

(defn menu-item
  "Returns the state needed to represent a menu item."
  [id label] (uc/initial-state MenuItem {:id id :label label}))

(def ui-menu-item (om/factory MenuItem {:keyfn :menu-item/id}))

(defui PopupMenu
  static uc/InitialAppState
  (initial-state [cls {:keys [id label items]}] {:menu/id id :menu/label label :menu/open? false :menu/items items})
  static om/IQuery
  (query [this] [:menu/id :menu/label {:menu/items (om/get-query MenuItem)} :menu/open?])
  static om/Ident
  (ident [this props] [:menus/by-id (:menu/id props)])
  Object
  (render [this]
    (let [{:keys [menu/id menu/label menu/items menu/open?]} (om/props this)
          onSelect (or (om/get-computed this :onSelect) identity)]
      (dom/div #js {:className (str "btn-group" (when open? " open"))}
        (dom/button #js {:type      "button" :data-toggle "dropdown" :aria-haspopup "true" :aria-expanded "false"
                         :onClick   (fn [evt]
                                      (if open?
                                        (om/transact! this `[(menu/close-all-menus) :menu/id])
                                        (om/transact! this `[(menu/close-all-menus) (menu/open-menu ~{:menu id}) :menu/id]))
                                      (.stopPropagation evt))
                         :className "btn btn-default dropdown-toggle"} label
                    (dom/span #js {:className "caret"}))
        (dom/ul #js {:className "dropdown-menu"}
                (map (fn [i] (ui-menu-item (om/computed i {:onSelect onSelect}))) items))))))

(def ui-popup (om/factory PopupMenu {:keyfn :menu/id}))

(defn set-popup
  "Set the state of the given menu to open (true) or closed (false)"
  [menu open?]
  (assoc menu :menu/open? open?))

(defmethod m/mutate 'menu/open-menu [{:keys [state]} k {:keys [menu]}]
  {:action #(swap! state update-in [:menus/by-id menu] set-popup true)})

(defmethod m/mutate 'menu/close-all-menus [{:keys [state]} k p]
  {:action (fn []
             (let [all-menu-ids (-> state deref :menus/by-id keys)]
               (swap! state
                 (fn [s] (reduce (fn [st menu-id]
                                   (update-in st [:menus/by-id menu-id] set-popup false)) s all-menu-ids)))))})

;; A simple simulation of an application with two menus
(defui ActiveMenuRoot
  static uc/InitialAppState
  (initial-state [cls {:keys [id label items]}]
    {:menu  (uc/initial-state PopupMenu {:id    :test-menu :label "Menu Label"
                                         :items [(menu-item :open "Open")
                                                 (menu-item :close "Close")
                                                 (menu-item :copy "Copy")]})
     :menu2 (uc/initial-state PopupMenu {:id    :test-menu-2 :label "Other Menu"
                                         :items [(menu-item :jump "Jump")
                                                 (menu-item :shout "Shout")
                                                 (menu-item :dance "Dance")]})})
  static om/IQuery
  (query [this] [{:menu (om/get-query PopupMenu)} {:menu2 (om/get-query PopupMenu)}])
  Object
  (render [this]
    (let [{:keys [menu menu2]} (om/props this)]
      (dom/div #js {:className "bg-info" :onClick #(om/transact! this '[(menu/close-all-menus)])}
        (ui-popup (om/computed menu {:onSelect (fn [id] (js/alert (str "You selected " id)))}))
        (ui-popup (om/computed menu2 {:onSelect (fn [id] (js/alert (str "You selected " id)))}))))))

(defcard popup-menu
  "A popup menu (closed)"
  (ui-popup {:menu/id :main-menu :menu/label "Main"}))

(defcard open-popup-menu
  "A popup menu (open)"
  (ui-popup {:menu/id :main-menu :menu/open? true :menu/label "Main" :menu/items [{:item/id :file :item/label "File"}]}))

(defcard active-popup-menu
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
  {:inspect-data false})                                    ; set to true to see app state
