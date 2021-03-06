(ns rulescript.lang.invocations
  (:require [clojure.spec.alpha :as s]
            [rulescript.io.error-catalogue :as errors]
            [rulescript.lang.utils :refer :all]))

(s/check-asserts true)

(s/def ::result-map (s/keys :req-un [::result ::rule]
                            :opt-un [::message]))
(s/def ::result #{:pass :fail :warn :error})
(s/def ::rule keyword?)
(s/def ::message string?)

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
;; after tagging, each rule application should return a result of shape 
;; {:result (:pass | :fail | :warn | :error)
;;  :rule   "rule name"  
;;;;;;;;;;;;;;;;;

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
    (let [tagged-rule-name# (if (:tag ~config)
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
  `(let [evaluated# (~application-fn ~rule-name ~@expressions)
         result-map# (application->result-map ~config ~rule-name evaluated#)]
     (try
       (do
         (s/assert ::result-map result-map#)
         result-map#)
       (catch clojure.lang.ExceptionInfo e#
         (errors/throw-ex-info :result-map-nonconform {:rule-name ~rule-name
                                                       :ex-info (ex-data e#)})))))

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

(defmacro change-result!
  "Change an entry in the :results map of env*"
  [rule-name-kw new-map]
  `(swap!
    ~'env*
    assoc-in
    [:results ~rule-name-kw]
    ~new-map))

(defmacro warn-when
  "If the change a result map's :result to :warn if it matches a given value."
  [warning-val rule-form]
  `(let [result-map# (eval ~rule-form)
         result-as-bool# (= :pass (:result result-map#))
         with-warning# (assoc result-map# :result :warn)]
     (if (= ~warning-val result-as-bool#)
       (do
         (change-result! (:rule result-map#) with-warning#)
         with-warning#)
       (do
         result-map#))))

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

(comment
  (use 'rulescript.lang.operations)

  (in {:age 10} find age)

  ((validate-document (inp)
                      (rule i-fail
                            (< 2 (in inp find age)))
                      (rule is-hello
                            (= 1 (in inp find age)))
                      (rule age-over-ten
                            (> 10 (in inp find age))))
   {:age 10})

  "end")
