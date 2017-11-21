(ns rulescript.lang.invocations
  (:require [rulescript.lang.utils :refer :all]))

(defn initialize-eval-env
  []
  (do
    (def env* (clojure.core/atom {:initial-ns *ns*
                                  :results    {}
                                  :vars       {}}))
    (remove-ns 'evalrules)
    (create-ns 'evalrules)
    (in-ns 'evalrules)
    (use 'rulescript.lang.invocations)
    (use 'rulescript.lang.operations)
    ))

(defn return-to-calling-ns
  []
  (-> (:initial-ns @env*)
      ns-name
      in-ns))

(defmacro validate-document
  "Top-level macro for rolling up the result of all rule applications."
  [[& inputs] & expressions]
  `(fn
     [~@inputs]
     (initialize-eval-env)
     ~@expressions
     (return-to-calling-ns)
     @env*
     ))


;;;;;;;;;;;;;;;;;
;; Wrapping rules (which usually produce predicates)
;; with structured return information.
;;;;;;;;;;;;;;;;;

#_(after tagging, each rule application should return a result of shape
         {:result (:pass | :fail | :warn | :error)
          :rule   "rule name"})

(defn stringify-fn
  [name]
  (->>
      (clojure.string/split name #"-")
      (map clojure.string/capitalize)
      (clojure.string/join " ")
      clojure.string/trim))

(defmacro log-application-result
  [result-map]
  `(swap! env*
          update-in
          [:results (:result ~result-map)]
          conj
          ~result-map))

(defmacro tag-rule-application
  "Wrap the application of a rule with structured rule output."
  [rule-name expressions]
  `(log-application-result
     (try
       (let [result# (if (map? ~expressions)
                       (:result ~expressions)
                       (if ~expressions :pass :fail))]
         {:result result#
          :rule   (stringify-fn (str ~rule-name))})
       (catch Exception e# {:result  :error
                            :rule    (stringify-fn (str ~rule-name))
                            :message (.getMessage e#)}))))

;;;;;;;;;;;;;;;;;
;; Defining and applying rules
;;;;;;;;;;;;;;;;;

(defmacro define-rule
  "Define a rule. Rules are closures expecting the arguments from arglist."
  [rule-name arglist & expressions]
  `(swap! env*
          assoc-in
          [:vars (symbol->keyword '~rule-name)]
          (fn
            [~@arglist]
            ~@expressions)))

(defmacro apply-rule-inner
  [rule-name & args]
  `(do
     ((get-in (deref env*)
              [:vars (symbol->keyword '~rule-name)])
       ~@args)))

(defmacro apply-rule
  "Apply a rule that has been defined."
  [rule-name & args]
  `(tag-rule-application '~rule-name
                         (apply-rule-inner ~rule-name ~@args)))


;;;;;;;;;;;;;;;;;
;; Apply a rule anonymously,
;; without defining a var for it.
;;;;;;;;;;;;;;;;;

(defmacro rule-inner
  [rule-name & expressions]
  `(do
     ~@expressions))

(defmacro rule
  "Execute a rule anonymously without variable bindings."
  [rule-name & expressions]
  `(tag-rule-application '~rule-name
                         (rule-inner ~rule-name ~@expressions)))


;;;;;;;;;;;;;;;;;
;; Warnings
;;;;;;;;;;;;;;;;;

(defn warn-if
  "Demote a 'passng' rule to a warning.
  Can only be used on result maps (e.g. when `rule` or `apply-rule` has been applied."
  [result-map]
  (if (not (map? result-map))
    {:result :error :rule "ERROR! warn-if should only be applied to rule or apply-rule."}
    (if (= :pass (:result result-map))
      (assoc result-map :result :warn)
      result-map)))


