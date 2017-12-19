(ns rulescript.io.main
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
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
    (str "Result generated " datestr "\n" result-str)) )

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
         add-timestamp)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; EVALUATING RULESCRIPTS FROM FILES
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- reader-from-str
  [filename extension]
  (let [xt (if (clojure.string/starts-with? extension ".")
             extension
             (str "." extension))]
    (-> (#(str "poli/" filename xt))
        io/resource
        io/reader)))

(defn- get-document
  "Read a JSON document by its name (excluding file extension)."
  ;; TODO graceful error handling when the file is not found.
  [doc-name]
  (cheshire/parse-stream (reader-from-str doc-name "json")
                true))

(defn- get-polispec
  "Read a named spec file (without file extension) into memory. The file still needs to be eval'd."
  ;; TODO graceful error handling when the file is not found.
  [spec-name]
  (let [spec-resource (reader-from-str spec-name "edn")]
    (with-open [in (PushbackReader. spec-resource)]
      (edn/read {:eof :eof} in))))

(defn eval-from-files
  [specname inputname & {:keys [pprint]}]
  (let [input (get-document inputname)
        spec (get-polispec specname)]
    ((eval spec) input)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; EVALUATING RULESCRIPTS FROM STRINGS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- eval-strings
  "Evaluate a rulescript spec and input, passed as strings."
  [spec input]
  (let [read-input (cheshire/parse-string input true)
        read-spec (edn/read-string spec)]
    ((eval read-spec) read-input)))

(defn eval-from-strings
  [spec input & {:keys [pprint]}]
  (let [output (eval-strings spec input)]
    (if pprint
      (pprint-results output)
      output)))


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
  (eval-from-strings
    "(validate-document (inp) (rule i-fail (< 2 (in inp find fail))) (rule is-hello (= 1 (in inp find age))) (rule age-over-ten (> 10 (in inp find age))))"
    "{\"age\":12}"
    :pprint true))
