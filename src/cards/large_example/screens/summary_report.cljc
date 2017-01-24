(ns large-example.screens.summary-report
  #?(:cljs (:require-macros
             [large-example.macros :refer [defmutation defroot]]))
  (:require
    #?(:clj [large-example.macros :refer [defmutation defroot]])
            [devcards.core :as dc :refer-macros [defcard]]
            [om.next :as om :refer [defui]]
            [untangled.client.cards :refer-macros [untangled-app]]
            [untangled.client.mutations :as m]
            [untangled.client.logging :as log]
            [untangled.i18n :refer-macros [tr trc]]
            [large-example.ui.screens.reports :as rpt]
            [om.dom :as dom]
            [untangled.client.core :as uc]))

(defroot Root rpt/SummaryReport)

#?(:cljs
   (defcard summary-report
     (untangled-app Root)
     {}
     {:inspect-data true}))
