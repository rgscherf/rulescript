(ns rulescript.lang.invocations-test
  (:require [clojure.set :as cset]
            [clojure.test :refer :all]
            [rulescript.lang.invocations :as i]))

(deftest init-env-test
  (let [env* (i/initialize-eval-env*)]
    (testing "New env vals are empty"
      (is (every? #(= {} %)
                  (vals @env*))))
    (testing "New env contains expected keys"
      (is (empty? (cset/difference
                   (-> @env* keys set)
                   #{:results :vars}))))))

(deftest define-rule-test
  (let [env* (i/initialize-eval-env*)]
    (i/define-rule testrule
      [hello]
      (= hello "hello"))
    (testing "Creates var at proper path"
      (is (not= nil (get-in @env* [:vars :testrule]))))
    (testing "Creates fn"
      (is (function? (get-in @env* [:vars :testrule]))))))

(deftest rule-test
  (let [env* (i/initialize-eval-env*)]
    (i/rule two-is-two
            (= 2 2))
    (let [res (-> @env* :results)]
      (testing "calling rule places named entry in results map"
        (is (not= nil (:two-is-two res))))
      (testing "calling rule creates map in results map"
        (is (map? (:two-is-two res))))
      (testing "calling rule creates properly-keyed map in results map"
        (let [this-result (:two-is-two res)]
          (is (= (-> this-result :rule)
                 :two-is-two))
          (is (= (-> this-result :result)
                 :pass)))))))

(deftest apply-rule-test
  (let [env* (i/initialize-eval-env*)]
    (i/define-rule testme
      [hello]
      (= hello "hello"))
    (i/apply-rule
     testme
     "failing"
     "goodbye")
    (i/apply-rule
     testme
     "failing"
     "what time is it?")
    (i/apply-rule
     testme
     "passing"
     "hello")
    (let [results (-> @env* :results)]
      (testing "duplicate tags overwrite results"
        (is (= (-> results keys count) 2)))
      (testing "failing key is result fail"
        (is (= (-> results :testme-failing :result) :fail)))
      (testing "passing key is result pass"
        (is (= (-> results :testme-passing :result) :pass))))))

(deftest warn-when-test
  (let [env* (i/initialize-eval-env*)]
    (i/warn-when
     true
     (i/rule should-be-true
             (= 1 1)))
    (let [results (-> @env* :results)]
      (testing "pass converted to warn"
        (is (= (-> results :should-be-true :result) :warn))))))
