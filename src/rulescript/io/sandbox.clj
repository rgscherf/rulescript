(ns rulescript.io.sandbox
  (:require
    [clojail.core :as clojail]
    [clojail.testers :as testers]))

(def rs-sandbox (clojail/sandbox
                  [(testers/blacklist-objects [clojure.lang.Compiler clojure.lang.Ref clojure.lang.Reflector
                                               clojure.lang.Namespace clojure.lang.Var clojure.lang.RT
                                               java.io.ObjectInputStream])
                   (testers/blacklist-packages ["java.lang.reflect"
                                                "java.security"
                                                "java.util.concurrent"
                                                "java.awt"])
                   (testers/blacklist-symbols
                     '#{alter-var-root intern
                        load-string load-reader addMethod ns-resolve resolve find-var
                        *read-eval* ns-publics ns-unmap set! ns-map ns-interns the-ns
                        push-thread-bindings pop-thread-bindings future-call agent send
                        send-off pmap pcalls pvals in-ns System/out System/in System/err
                        with-redefs-fn Class/forName})
                   (testers/blacklist-nses '[clojure.main])
                   (testers/blanket "clojail")]))

