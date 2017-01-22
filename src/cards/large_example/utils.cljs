(ns large-example.utils
  (:require [devcards.core :as dc :include-macros true]
            [clojure.set :as set]
            [clojure.string :as str]
            [hickory.core :as hc]
            [untangled.client.mutations :as m]
            [untangled.client.core :as uc]
            [om.next :as om :refer [defui]]
            [om.dom :as dom]
            [devcards.util.markdown :as md]
            [devcards.util.edn-renderer :as edn]))

(def attr-renames {
                   :class        :className
                   :for          :htmlFor
                   :tabindex     :tabIndex
                   :viewbox      :viewBox
                   :spellcheck   :spellcheck
                   :autocorrect  :autoCorrect
                   :autocomplete :autoComplete
                   })

(defn elem-to-cljs [elem]
  (cond
    (string? elem) elem
    (vector? elem) (let [tag (name (first elem))
                         attrs (set/rename-keys (second elem) attr-renames)
                         children (map elem-to-cljs (rest (rest elem)))]
                     (concat (list (symbol "dom" tag) (symbol "#js") attrs) children))
    :otherwise "UNKNOWN"))

(defn to-cljs
  "Convert an HTML fragment (containing just one tag) into a corresponding Om Dom cljs"
  [html-fragment]
  (let [hiccup-list (map hc/as-hiccup (hc/parse-fragment html-fragment))]
    (first (map elem-to-cljs hiccup-list))))

(defmethod m/mutate 'convert [{:keys [state]} k p]
  {:action (fn []
             (let [html (get-in @state [:top :conv :html])
                   cljs (to-cljs html)]
               (swap! state assoc-in [:top :conv :cljs] cljs)))})

(defui HTMLConverter
  static uc/InitialAppState
  (initial-state [clz params] {:html "<div> </div>" :cljs (list)})
  static om/IQuery
  (query [this] [:cljs :html])
  static om/Ident
  (ident [this p] [:top :conv])
  Object
  (render [this]
    (let [{:keys [html cljs]} (om/props this)]
      (dom/div #js {:className ""}
        (dom/textarea #js {:cols     80 :rows 10
                           :onChange (fn [evt] (m/set-string! this :html :event evt))
                           :value    html})
        (dom/div #js {} (edn/html-edn cljs))
        (dom/button #js {:className "c-button" :onClick (fn [evt]
                                                          (om/transact! this '[(convert)]))} "Convert")))))

(def ui-html-convert (om/factory HTMLConverter))

(defui HTMLConverterApp
  static uc/InitialAppState
  (initial-state [clz params] {:converter (uc/initial-state HTMLConverter {})})
  static om/IQuery
  (query [this] [{:converter (om/get-query HTMLConverter)} :react-key])
  static om/Ident
  (ident [this p] [:top :conv])
  Object
  (render [this]
    (let [{:keys [converter ui/react-key]} (om/props this)]
      (dom/div
        #js {:key react-key} (ui-html-convert converter)))))





