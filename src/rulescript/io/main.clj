(ns rulescript.io.main
  (:require
    [rulescript.io.sandbox :as sandbox]
    [rulescript.lang.utils :refer :all]
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.string :as string]
    [cheshire.core :as cheshire])
  (:import (java.io PushbackReader)
           (java.text SimpleDateFormat)
           (java.util Date)))


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


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; EVALUATING RULESCRIPTS FROM FILES
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
  [specname inputname source]
  (let [input (if (= source :strings)
                (cheshire/parse-string inputname true)
                (read-json-file inputname))
        spec (try (sandbox/rs-sandbox (if (= source :strings)
                                        (edn/read-string specname)
                                        (read-rulescript-file specname)))
                  (catch java.lang.SecurityException e (throw e))
                  (catch java.util.concurrent.ExecutionException e (if (= source :strings)
                                                                     (edn/read-string specname)
                                                                     (read-rulescript-file specname))))]
    @(future
       ((eval spec) input))))

(defn eval-rules
  [spec input & {:keys [pprint source]}]
  (let [output (eval-rules* spec input source)]
    (if pprint
      (pprint-results output)
      (cheshire/generate-string output))))

(defn eval-from-strings
  [spec input & {:keys [pprint] :or {pprint true}}]
  (eval-rules spec input :pprint pprint :source :strings))

(defn eval-from-files
  [spec input & {:keys [pprint printonly] :or {pprint true printonly false}}]
  (let [result (eval-rules spec input :pprint pprint :source :files)]
    (if printonly
      (println result)
      result)))


(comment
  (let [testgroup {:age-over-ten      {:rule :age-over-ten, :result :fail}
                   :i-fail            {:rule :i-fail, :result :error, :message nil},
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
   (eval-from-files "./resources/drao" "./resources/drao" :pprint true)
   (eval-from-strings
           "(validate-document (inp) (rule i-fail (< 2 (in inp find fail))) (rule is-hello (= 1 (in inp find age))) (rule age-over-ten (> 10 (in inp find age))))"
           "{\"age\":12}"
           :pprint true))
