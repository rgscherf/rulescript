(ns rulescript.io.error-catalogue)

(def errors
  {:spec-arity
   (fn [e] {:cause "Wrong number of inputs to spec."
            :error (.getMessage e)
            :remedy "Check the number of inputs your spec is expecting. The number of inputs in your spec must match the number of inputs you provide during validation. Inputs to a spec are written at the top, right after 'validate document ...'."})
   :evaluation-timeout
   (fn [timeout] {:cause "Your spec took too long to evaluate."
                  :error (str "Execution took longer than current timeout value (" timeout " miliseconds)")
                  :remedy "RuleScript automatically stops spec execution after a certain amount of time. This should be more than long enough to evaluate any normal spec. It's likely that you (1) caused an infinite loop or (2) triggered an extremely long-running calculation. Review your spec and try again. If you require a longer execution timeout and are using RuleScript from Clojure or the command line, explore documentation related to the :timeout keyword argument."})
   :forbidden-symbol
   (fn [e] {:cause "Your spec used a forbidden word."
            :error (.getMessage e)
            :remedy "RuleScript forbids the use of certain Clojure symbols (commands) for security purposes. The full list of forbidden symbols can be found at rulescript.org/docs. If you accidentally used one of these symbols in your spec, simply rename that symbol. If you're an experienced Clojure user, learn more about the Clojail sandboxing library at https://github.com/Raynes/clojail."})})

(defn throw-ex-info
  "Throw an ex exception."
  ;; ... with error info map key and the exception:
  ([error-key extra-arg]
   (let [{:keys [cause error remedy other]} ((get errors error-key) extra-arg)]
     (throw-ex-info cause error remedy other)))
  ;; ... or cause, error, and remedy messages, plus any additional map entries:
  ([cause-msg error-msg remedy-msg other-info]
   (throw (ex-info "Error while evaluating spec."
                   {:cause cause-msg
                    :other other-info
                    :remedy remedy-msg
                    :error error-msg}))))

(defn pprint-error
  [error]
  (let [{:keys [cause error remedy other]} (ex-data error)
        banner-msg "SPEC EVALUATION ERROR: "
        line (apply str (-> (apply + (map count [banner-msg cause]))
                            (repeat "=")
                            vec
                            (conj "\n")))]
    (str line
         banner-msg
         cause "\n"
         line
         remedy "\n\n"
         "Additional info from Clojure: " error "\n"
         (if other
           (str "Other data: " other "\n")
           ""))))
