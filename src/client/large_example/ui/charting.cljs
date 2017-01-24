(ns large-example.ui.charting
  (:require cljsjs.victory))

(defn factory-apply
   [class]
        (fn [props & children]
          (apply js/React.createElement
                 class
                 props
                 children)))

(def vcontainer (factory-apply js/Victory.VictoryContainer))
(def vchart (factory-apply js/Victory.VictoryChart))
(def vaxis (factory-apply js/Victory.VictoryAxis))
(def vline (factory-apply js/Victory.VictoryLine))
(def vbar (factory-apply js/Victory.VictoryBar))


