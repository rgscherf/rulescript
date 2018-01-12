(ns rulescript.laboratory
  (:require [clojure.java.io :as jio]
            [clojure.edn :as edn]
            [rulescript.lang.operations :refer :all]
            [rulescript.lang.invocations :refer :all]
            [rulescript.io.main :refer :all]
            [cheshire.core :as cheshire]))

(eval-from-strings
  "(validate-document (inp) (rule i-fail (< 2 (in inp find fail))) (rule is-hello (= 1 (in inp find age))) (rule age-over-ten (> 10 (in inp find age))))"
  "{\"age\":12}"
  :pprint true)

(defn book
  [title author overdue fines]
  {:title   title
   :author  author
   :overdue overdue
   :fines   fines})

(def application
  {:type-of-application "Library Card Renewal"
   :name                "Harold Harnold"
   :address             {:city     "Hamilton"
                         :province "Ontario"}
   :current-member      true
   :times-renewed       1
   :old-fines           2.40
   :checkouts           [(book "Capital in the Twenty-First Century"
                               "Thomas Piketty"
                               false
                               0)
                         (book "Enlightenment 2.0: Restoring Sanity to Our Politics, Our Economy, and Our Lives"
                               "Joseph Heath"
                               false
                               0)
                         (book "What Is Government Good At?: A Canadian Answer"
                               "Donald J. Savoie"
                               false
                               0)
                         (book "The Little Schemer"
                               "Daniel P. Friedman and Matthias Felleisen"
                               true
                               0.90)
                         (book "Jampires"
                               "David O'Connell and Sarah McIntyre"
                               false
                               0)

                         ]})

(def drao-app
  (-> "drao.json"
      jio/resource
      slurp
      (cheshire/parse-string keyword)))

(def spec
  (-> "drao-full.edn"
      jio/resource
      slurp
      edn/read-string))

((eval spec) drao-app)




(complete? [:a :b :c])
(complete? '(:a :b :c))
(complete? #{:a :b :c})