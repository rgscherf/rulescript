(ns rulescript.io.main
  (:require
   [rulescript.io.sandbox :as sandbox]
   [rulescript.lang.utils :refer :all]
   [rulescript.io.error-catalogue :as errors]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [cheshire.core :as cheshire])
  (:import (java.io PushbackReader)
           (java.text SimpleDateFormat)
           (java.util Date)
           (java.util.concurrent TimeoutException ExecutionException)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PRETTY PRINTING RESULTS AS STRING
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- print-result-class
  [[class-seq label]]
  (if class-seq
    (apply
     (partial str
              "=======\n"
              label
              "\n"
              "=======\n")
     (map #(-> %
               :rule
               name
               stringify-fn
               (str "\n"))
          class-seq))))

(defn- add-timestamp
  "Add datestamp to a string."
  [result-str]
  (let [datestr (-> (SimpleDateFormat. "y-MM-dd h:mm:ss a z")
                    (.format (Date.)))]
    (str "Result generated " datestr "\n" result-str)))

(defn- pprint-results
  "Pretty print the results of a rulescript evaluation."
  [results]
  (let [{:keys [error pass fail warn]} (->> results
                                            vals
                                            (group-by :result))]
    (->> [[error "ERROR"]
          [pass "PASS"]
          [warn "WARN"]
          [fail "FAIL"]]
         (map print-result-class)
         (map #(str % "\n"))
         (reduce str "")
         add-timestamp
         string/trimr)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DEFAULT EVALUATION OPTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def default-opts
  {:pprint    true
   :timeout   5000
   :printonly false})

;;;;;;;;;
;; IO OPS
;;;;;;;;;

(defn- reader-from-str
  [filename extension]
  (let [xt (if (clojure.string/starts-with? extension ".")
             extension
             (str "." extension))]
    (io/reader (str filename xt))))

(defn- read-json-file
  "Read a JSON document by its name (excluding file extension)."
  ;; TODO graceful error handling when the file is not found.
  [doc-name]
  (cheshire/parse-stream (reader-from-str doc-name "json")
                         true))

(defn- read-rulescript-file
  "Read a named spec file (without file extension) into memory. The file still needs to be eval'd."
  ;; TODO graceful error handling when the file is not found.
  [spec-name]
  (let [spec-resource (reader-from-str spec-name "edn")]
    (with-open [in (PushbackReader. spec-resource)]
      (edn/read {:eof :eof} in))))

;;;;;;;;;;;;;;;;;;;;;;;;;
;; EVALUATING RULESCRIPTS
;;;;;;;;;;;;;;;;;;;;;;;;;


(defn- eval-rules*
  "Evaluate a rulescript spec and input. Source is either :string or :file.
  Disallows certain symbols, defined in ns rulescript.io.sandbox"
  [{:keys [source]} specname & inputnames]
  (let [inputs (map (if (= source :strings)
                      #(cheshire/parse-string % true)
                      read-json-file)
                    inputnames)
        spec (try (sandbox/rs-sandbox (if (= source :strings)
                                        (edn/read-string specname)
                                        (read-rulescript-file specname)))
                  (catch SecurityException e (errors/throw-ex-info :forbidden-symbol e))
                  (catch ExecutionException e (if (= source :strings)
                                                (edn/read-string specname)
                                                (read-rulescript-file specname))))]
    (try
      (apply (eval spec) inputs)
      (catch clojure.lang.ArityException e (errors/throw-ex-info :spec-arity e)))))

(defn print-error
  [pprint error]
  (let [err-string (errors/pprint-error error)]
    (if pprint
      (println err-string)
      (cheshire/generate-string err-string))))

(defn- eval-rules
  "Outer evaluation, controlling eval timeout and printing of results."
  [{:keys [pprint timeout] :as opts} spec & inputs]
  (try
    (let [output (deref
                  (future (apply eval-rules* opts spec inputs))
                  timeout
                  :too-long)]
      (if (= output :too-long)
        (errors/throw-ex-info :evaluation-timeout timeout))
      (if pprint
        (pprint-results output)
        (cheshire/generate-string output)))
    (catch clojure.lang.ExceptionInfo e
      (print-error pprint e))
    (catch java.util.concurrent.ExecutionException e
      (print-error pprint (.getCause e)))))

(defn eval-from-strings
  "Evaluate RuleScript with string inputs. Can take an option map as first argument. See default-opts in this NS."
  ([opts spec & inputs]
   (apply eval-rules (merge-ignore-nil default-opts
                                       opts
                                       {:source :strings})
          spec
          inputs)))

(defn eval-from-files
  "Evaluate RuleScript with file path inputs. Can take an option map as first argument. See default-opts in this NS."
  ([opts spec & inputs]
   (let [eval-opts (merge-ignore-nil default-opts opts {:source :files})
         result (apply eval-rules eval-opts spec inputs)]
     (if (:printonly opts)
       (println result)
       result))))

(comment
  (let [testgroup {:age-over-ten      {:rule :age-over-ten, :result :fail}
                   :i-fail            {:rule :i-fail, :result :error, :message nil}
                   :age-under-fifteen {:rule :age-under-fifteen, :result :pass}
                   :is-a-dog          {:rule :is-a-dog, :result :fail}
                   :lives-in-toronto  {:rule :lives-in-toronto, :result :warn}
                   :lives-in-ontario  {:rule :lives-in-ontario, :result :pass}
                   :has-a-car         {:rule :has-a-car, :result :fail}}]
    (pprint-results testgroup)))

(comment

  (use 'rulescript.lang.invocations)
  (use 'rulescript.lang.operations)

  (let [spec (read-rulescript-file "./resources/drao")
        application (read-json-file "./resources/drao")]
    (pprint-results ((eval spec) application)))

  (eval-from-files {:printonly true} "./resources/drao" "./resources/drao")

  (sandbox/rs-sandbox (read-rulescript-file "./resources/drao"))

  (eval-from-strings
   {}
   "(validate-document (inp) (rule i-fail (< 2 (in inp find age))) (rule is-hello (= 1 (in inp find age))) #_(rule second-age-under-two (> 3 (in seco find secondage))))"
   "{\"age\":12}"
   "{\"secondage\":12}"))
