(ns large-example.ui.components.menus
  (:require-macros
    [large-example.macros :refer [defmutation]])
  (:require [om.next :as om :refer-macros [defui]]
            [untangled.client.cards :refer-macros [untangled-app]]
            [untangled.client.mutations :as m]
            [om.dom :as dom]
            [untangled.client.core :as uc]))

(declare ui-menu-item close-all-menus open-menu)

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
                         (om/transact! this `[(close-all-menus {}) :menu/id])
                         (onSelect id))]
      (dom/li nil
              (dom/a #js {:onClick clickHandler} label)))))

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
                                        (om/transact! this `[(close-all-menus {}) :menu/id])
                                        (om/transact! this `[(close-all-menus {}) (open-menu ~{:menu id}) :menu/id]))
                                      (.stopPropagation evt))
                         :className "btn btn-default dropdown-toggle"} label
                    (dom/span #js {:className "caret"}))
        (dom/ul #js {:className "dropdown-menu"}
                (map (fn [i] (ui-menu-item (om/computed i {:onSelect onSelect}))) items))))))

(def ui-popup
  "Make a react component that renders a popup-menu."
  (om/factory PopupMenu {:keyfn :menu/id}))

(def ui-menu-item
  "Make a react component that renders a menu item"
  (om/factory MenuItem {:keyfn :menu-item/id}))

(defn make-popup-menu
  "Create the state for a popup menu. The items parameter should be a vector of `menu-item`."
  [id label items]
  (uc/get-initial-state PopupMenu {:id id :label label :items items}))

(defn make-menu-item
  "Returns the state needed to represent a menu item."
  [id label] (uc/get-initial-state MenuItem {:id id :label label}))

(defn set-popup
  "Set the visible state of the given menu items to :open or :closed."
  [menu open?]
  (assoc menu :menu/open? (or (identical? true open?) (= :open open?))))

(defmutation open-menu
  "Om Mutation: Open a popup menu. Params should include a :menu key with the ID of the menu to open."
  [menu]
  (swap! state update-in [:menus/by-id menu] set-popup true))

(defmutation close-all-menus
  "Om Mutation: Close all open menus in the entire application. No parameters required.`"
  [ignored]
  (let [all-menu-ids (-> state deref :menus/by-id keys)]
    (swap! state
           (fn [s] (reduce (fn [st menu-id]
                             (update-in st [:menus/by-id menu-id] set-popup false)) s all-menu-ids)))))

