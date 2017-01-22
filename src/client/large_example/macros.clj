(ns large-example.macros
  (:require [clojure.spec :as s]))

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

(comment
  (macroexpand-1 '(defmutation open "This is a test" [id param] (println "hello") (println "there")))
  (macroexpand-1 '(defmutation open "This is a test" [id param] (println hello) true))

  )
