(ns rulescript.lang.invocations
  (:require [rulescript.lang.utils :refer :all]))


(defn reset-ns
  []
  (do
    (remove-ns 'evalrules)
    (create-ns 'evalrules)
    (in-ns 'evalrules)
    (use 'rulescript.lang.invocations)
    (use 'rulescript.lang.operations)
    (def env* (clojure.core/atom {}))))

(defmacro validate-document
  "Top-level macro for rolling up the result of all rule applications."
  [[& inputs] & expressions]
  `(fn
     [~@inputs]
     (reset-ns)
     ~expressions
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
  (-> name
      (clojure.string/split #"-")
      (interleave (repeat " "))
      (clojure.string/join)))

(defmacro tag-rule-application
  "Wrap the application of a rule with structured rule output."
  [rule-name expressions]
  `(println (try
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
          assoc
          (symbol->keyword '~rule-name)
          (fn
            [~@arglist]
            ~@expressions)))

(defmacro apply-rule-inner
  [rule-name _ & args]
  `(println (keys (deref 'env*)))

  #_((get env*
          (symbol->keyword ~rule-name))
      (into [] (if (= 1 (count '~args))
                 '~args
                 (flatten '~args)))))

(defmacro apply-rule
  "Apply a rule that has been defined."
  [rule-name _ & args]
  `(tag-rule-application ~rule-name
                         (apply-rule-inner ~rule-name 'to ~@args)))


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


