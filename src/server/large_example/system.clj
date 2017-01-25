(ns large-example.system
  (:require
    [untangled.server.core :as core]
    [com.stuartsierra.component :as component]

    [om.next.server :as om]
    [large-example.api.read :as r]
    [large-example.api.mutations :as mut]
    [untangled.server.impl.components.handler :as h]

    [taoensso.timbre :as timbre]
    [ring.middleware.cookies :as cookies]
    [ring.middleware.resource :as resource]
    [ring.mock.request :as req]
    [ring.util.response :as response]))

(defn logging-mutate [env k params]
  (timbre/info "Entering mutation:" k)
  (mut/apimutate env k params))

; TEMPLATE: :examples
(defrecord AdditionalPipeline [handler]
  component/Lifecycle
  (start [this]
    ; This is the basic pattern for composing into the existing pre-hook handler (which starts out as identity)
    ; If you're sure this is the only component hooking in, you could simply set it instead.
    (let [vanilla-pipeline (h/get-fallback-hook handler)
          html5-routing-handler (fn [handler]
                                  (fn [req]
                                   (.println System/out (str "REQUEST" req))
                                   (let [headers (:headers req)]
                                     (-> (resource/resource-request (-> (req/request :get "/index-dev.html")
                                                                     (assoc :headers headers)) "public")
                                         (response/content-type "text/html")))))]
      (h/set-fallback-hook! handler (comp vanilla-pipeline
                                          (partial html5-routing-handler))))
    this)
  (stop [this] this))

(defn make-system [config-path]
  (core/make-untangled-server
    :config-path config-path
    :parser (om/parser {:read r/api-read :mutate logging-mutate})
    ; TEMPLATE: :examples
    :components {:pipeline (component/using                 ; add some additional wrappers to the pipeline
                             (map->AdditionalPipeline {})
                             [:handler])}))
