(ns large-example.html-conversion
  (:require [devcards.core :as dc :refer-macros [defcard]]
            [om.next :as om :refer-macros [defui]]
            [large-example.utils :as u]
            [untangled.client.cards :refer-macros [untangled-app]]
            [om.dom :as dom]))

(defcard html-conversion
  "# An HTML to React Converter"
  (untangled-app u/HTMLConverterApp))
