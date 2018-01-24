(ns rulescript.lang.invocations
  (:require
    [rulescript.lang.utils :refer :all]))

(defn initialize-eval-env
  "Set up execution evironment for rule evaluation."
  []
  (def env* (clojure.core/atom {:results {}
                                :vars    {}})))

(defmacro validate-document
  "Top-level macro for rolling up the result of all rule applications."
  [[& inputs] & expressions]
  `(fn
     [~@inputs]
     (initialize-eval-env)
     ~@expressions
     (let [res# (:results @env*)]
       res#)))


;;;;;;;;;;;;;;;;;
;; Wrapping rules (which usually produce predicates)
;; with structured return information.
;;;;;;;;;;;;;;;;;

#_(after tagging, each rule application should return a result of shape
         {:result (:pass | :fail | :warn | :error)
          :rule   "rule name"})

(defmacro log-application-result!
  "Associate the result of an application in the :results map of env*."
  [result-map]
  `(do (swap! env*
              assoc-in
              [:results (:rule ~result-map)]
              ~result-map)
       ~result-map))

(defmacro application->result-map
  "Wrap the application of a rule with structured rule output."
  [rule-name expressions]
  `(log-application-result!
     (try
       (let [result# (if (map? ~expressions)
                       (:result ~expressions)
                       (if ~expressions :pass :fail))]
         {:result result#
          :rule   (symbol->keyword ~rule-name)})
       (catch Exception e# {:result  :error
                            :rule    (symbol->keyword ~rule-name)
                            :message (.getMessage e#)}))))

;;;;;;;;;;;;;;;;;;
;; RULE DEFINITION
;;;;;;;;;;;;;;;;;;

(defmacro define-rule
  "Define a rule. Rules are closures expecting the arguments from arglist."
  [rule-name arglist & expressions]
  `(swap! env*
          assoc-in
          [:vars (symbol->keyword '~rule-name)]
          (fn
            [~@arglist]
            ~@expressions)))

;;;;;;;;;;;;;;;;;;;
;; RULE APPLICATION
;;;;;;;;;;;;;;;;;;;

(defmacro apply-rule*
  "Pass a rule and its expressions off to be tagged in env*"
  [application-method rule-name & expressions]
  `(application->result-map ~rule-name (~application-method ~rule-name ~@expressions)))

(defmacro apply-rule-inner
  "Get a fn by rule-name from the vars map, and apply it to args"
  [rule-name & args]
  `(do
     ((get-in (deref env*)
              [:vars (symbol->keyword ~rule-name)])
       ~@args)))

(defmacro apply-rule
  "Apply a rule that has been defined."
  [rule-name & expressions]
  `(apply-rule* 'apply-rule-inner '~rule-name '~@expressions))

(defmacro rule-inner
  "Execute expressions."
  [rule-name & expressions]
  `(do
     ~@expressions))

(defmacro rule
  "Execute a rule anonymously without variable bindings."
  [rule-name & expressions]
  `(apply-rule* 'rule-inner '~rule-name ~@expressions))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PROMOTE/DEMOTE RESULTS TO WARNING
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn change-result!
  "Change an entry in the :results map of env*"
  [rule-name-kw new-map]
  (swap!
    env*
    assoc-in
    [:results rule-name-kw]
    new-map))

(defn warn-when
  "If the change a result map's :result to :warn if it matches a given value."
  [warning-val {:keys [rule result] :as result-map}]
  (let [result-as-bool (= :pass result)
        with-warning (assoc result-map :result :warn)]
    (if (= warning-val result-as-bool)
      (do
        (try
          (change-result! rule with-warning)
          (catch ClassCastException e (println (.getMessage e))))
        with-warning)
      result-map)))

