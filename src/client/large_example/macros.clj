(ns large-example.macros
  (:require [clojure.spec :as s]
            [untangled.client.core :as uc]
            [om.next :as om]
            [om.dom :as dom]
            [untangled.client.logging :as log]))

(s/def ::mutation-args (s/cat
                         :sym symbol?
                         :doc (s/? string?)
                         :arglist vector?
                         :body (s/+ (constantly true))))

(defn conform! [spec x]
  (let [rt (s/conform spec x)]
    (when (s/invalid? rt)
      (throw (ex-info (s/explain-str spec x)
                      (s/explain-data spec x))))
    rt))

(defmacro ^{:doc      "Define an Untangled mutation.

The given symbol will be prefixed with the namespace of the current namespace.

The arglist should be the *parameter* arglist of the mutation, NOT the complete argument list
for the equivalent defmethod. For example:

   (defmutation boo [id] ...) => (defmethod m/mutate *ns*/boo [{:keys [state ref]} _ {:keys [id]}] ...)

The following will be available in the body:
- state : The application state atom
- ref : The ident of the invoking component, if available
- ast : The AST of the mutation
- env : The complete Om Parser env
"
            :arglists '([sym docstring? arglist remote-body? body])} defmutation [& args]
  (let [{:keys [sym doc arglist body]} (conform! ::mutation-args args)
        fqsym (symbol (name (ns-name *ns*)) (name sym))
        env '{:keys [state ref ast] :as env}]
    `(defmethod untangled.client.mutations/mutate '~fqsym [~env ~'_ {:keys ~arglist}]
       {:action (fn [] ~@body)})))

(om/defui Screen1
  static untangled.client.core/InitialAppState
  (initial-state [clz params] {:page :screen1})
  static om.next/IQuery
  (query [this] [:db/id :screen1/label :page])
  Object
  (render [this] (dom/div nil "TODO")))

(om/defui Screen2
  static untangled.client.core/InitialAppState
  (initial-state [clz params] {:page :screen2})
  static om.next/IQuery
  (query [this] [:db/id :screen1/label :page])
  Object
  (render [this] (dom/div nil "TODO")))

(s/def ::router-args (s/cat
                       :sym symbol?
                       :ident-fn (constantly true)
                       :children (s/+ symbol?)))

(defmacro ^{:doc      "Generates a component with a union query that can route among the given screen, which MUST be
in cljc files. The first screen listed will be the 'default' screen that the router will be initialized to show.

- All screens *must* be in cljc files
- All screens *must* implement InitialAppState
- All screens *must* have a UI query
"
            :arglists '([sym ident-fn & screens])} defrouter [& args]
  (let [{:keys [sym ident-fn children]} (conform! ::router-args args)
        id (eval ident-fn)
        comp->kw (fn [comp] (first (id nil (untangled.client.core/get-initial-state (var-get (ns-resolve *ns* comp)) {}))))
        query (reduce (fn [q cls] (assoc q (comp->kw cls) `(om.next/get-query ~cls))) {} children)
        first-screen (first children)
        screen-render (fn [cls] `((om.next/factory ~cls {:keyfn (fn [props#] ~(name cls))}) (om.next/props ~'this)))
        render-stmt (reduce (fn [cases cls]
                              (-> cases
                                  (conj (comp->kw cls) (screen-render cls)))) [] children)]
    `(om.next/defui ~sym
       ~'static untangled.client.core/InitialAppState
       (~'initial-state [~'clz ~'params] (untangled.client.core/get-initial-state ~first-screen ~'params))
       ~'static om.next/Ident
       (~'ident [~'this ~'props] (~ident-fn ~sym ~'props))
       ~'static om.next/IQuery
       (~'query [~'this] ~query)
       ~'Object
       (~'render [~'this]
         (log/info (om/get-ident ~'this))
         (let [page# (first (om.next/get-ident ~'this))]
           (case page#
             ~@render-stmt
             (om.dom/div nil (str "Cannot route: Unknown Screen " page#))))))))

(comment
  (macroexpand-1 '(defrouter TopRouter (fn [cls props] [(:page props) :report]) Screen1 Screen2))
  (macroexpand-1 '(defmutation open "This is a test" [id param] (println "hello") (println "there")))
  (macroexpand-1 '(defmutation open "This is a test" [id param] (println hello) true)))

