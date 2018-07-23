(ns rulescript.lang.invocations-test
  (:require [clojure.set :as cset]
            [clojure.test :refer :all]
            [rulescript.lang.invocations :as i]))

(deftest init-env
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
