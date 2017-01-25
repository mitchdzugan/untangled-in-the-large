(ns large-example.api.read
  (:require
    ;[untangled.datomic.protocols :as udb]
    [large-example.api.mutations :as m]
    [taoensso.timbre :as timbre]))

(timbre/info "Loading API definitions for large-example.api.read")


(defn api-read [{:keys [query request] :as env} disp-key params]
  ;(let [connection (udb/get-connection survey-database)])
  (case disp-key
    (throw (ex-info "Invalid request" {:query query :key disp-key}))))
