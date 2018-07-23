(ns rulescript.lang.invocations
  (:require
   [rulescript.lang.utils :refer :all]))

#_(defn initialize-eval-env
    "Set up execution evironment for rule evaluation."
    []
    (def env* (clojure.core/atom {:results {}
                                  :vars    {}})))

(defn initialize-eval-env*
  []
  (clojure.core/atom {:results {}
                      :vars {}}))

(defmacro validate-document
  "Top-level macro for rolling up the result of all rule applications."
  [[& inputs] & expressions]
  `(fn
     [~@inputs]
     (let [~'env* (initialize-eval-env*)]
       ~@expressions
       (let [res# (:results (deref ~'env*))]
         res#))))

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
  `(do (swap! ~'env*
              assoc-in
              [:results (:rule ~result-map)]
              ~result-map)
       ~result-map))

(defmacro application->result-map
  "Wrap the application of a rule with structured rule output."
  [config rule-name eval-result]
  `(log-application-result!
    (let [tagged-rule-name# (if  (:tag ~config)
                              (keyword (str ~rule-name "-" (-> ~config :tag despace)))
                              (symbol->keyword ~rule-name))]
      (try
        (let [result# (if (map? ~eval-result)
                        (:result ~eval-result)
                        (if ~eval-result :pass :fail))]
          {:result result#
           :rule   tagged-rule-name#})
        (catch Exception e# {:result  :error
                             :rule    tagged-rule-name#
                             :message (.getMessage e#)})))))

;;;;;;;;;;;;;;;;;;
;; RULE DEFINITION
;;;;;;;;;;;;;;;;;;

(defmacro define-rule
  "Define a rule. Rules are closures expecting the arguments from arglist."
  [rule-name arglist & expressions]
  `(swap! ~'env*
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
  [application-fn config rule-name & expressions]
  `(let [evaluated# (~application-fn ~rule-name ~@expressions)]
     (application->result-map ~config ~rule-name evaluated#)))

(defmacro apply-rule-inner
  "Get a fn by rule-name from the vars map, and apply it to args"
  [rule-name & args]
  `(let [eval-result# ((get-in (deref ~'env*)
                               [:vars (symbol->keyword ~rule-name)])
                       ~@args)]
     eval-result#))

(defmacro apply-rule
  "Apply a rule that has been defined."
  [rule-name tag & expressions]
  `(apply-rule* apply-rule-inner {:tag ~tag} '~rule-name ~@expressions))

(defmacro rule-inner
  "Execute expressions."
  [rule-name & expressions]
  `(do
     ~@expressions))

(defmacro rule
  "Execute a rule anonymously without variable bindings."
  [rule-name & expressions]
  `(apply-rule* rule-inner {} '~rule-name ~@expressions))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PROMOTE/DEMOTE RESULTS TO WARNING
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn change-result!
  "Change an entry in the :results map of env*"
  [rule-name-kw new-map]
  (swap!
   ~'env*
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

(comment
  ;; capturing lexically-scoped values in a macro
  ;; see https://stackoverflow.com/questions/33471659/how-to-capture-in-lexical-scope-in-a-clojure-macro
  ;; can't use bare symbol. Have to quote and then eval the quoted symbol: ~'sym

  (defmacro addition []
    `(+ 1 ~'macro-val))

  (let [macro-val 1]
    (addition))

  (macroexpand '(addition))

  "end comment")
