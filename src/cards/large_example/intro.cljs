(ns large-example.intro
  (:require [devcards.core :as rc :refer-macros [defcard]]
            [om.next :as om :refer-macros [defui]]
            [large-example.ui.components.placeholder :as p]
            [om.dom :as dom]))

(defcard SVGPlaceholder
         "# SVG Placeholder"
         (p/ui-placeholder {:w 200 :h 200}))
