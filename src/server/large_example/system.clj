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
    [ring.util.request :as rreq]
    [ring.util.response :as response]
    [clojure.string :as str]))

(defn logging-mutate [env k params]
  (timbre/info "Entering mutation:" k)
  (mut/apimutate env k params))

(defn wrap-html5-routes-as-index [handler]
  (fn [req]
    (let [headers (:headers req)
          url (rreq/request-url req)
          dev-mode? (boolean (System/getProperty "dev"))
          ; only serve index in place of things that do not have a suffix, or end in .html
          is-leaf? (boolean (re-matches #".*/([^/.]*|[^/.]*\.html)$" url))]
      (if is-leaf?
        (-> (resource/resource-request (-> (req/request :get (if dev-mode? "/index-dev.html" "/index.html"))
                                           (assoc :headers headers)) "public")
            (response/content-type "text/html"))
        (handler req)))))

; TEMPLATE: :examples
(defrecord AdditionalPipeline [handler]
  component/Lifecycle
  (start [this]
    ; This is the basic pattern for composing into the existing pre-hook handler (which starts out as identity)
    ; If you're sure this is the only component hooking in, you could simply set it instead.
    (let [vanilla-pipeline (h/get-fallback-hook handler)]
      (h/set-fallback-hook! handler (comp vanilla-pipeline
                                          (partial wrap-html5-routes-as-index))))
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
