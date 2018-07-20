(ns rulescript.lang.invocations-test
  (:require [clojure.test :as t]
            [clojure.set :as cset]
            [rulescript.lang.invocations :as i]))

(t/deftest init-env
  (t/testing "New env vals are empty"
    (t/is (every? #(= {} %)
                  (vals @env*))))
  (t/testing "New env contains expected keys"
    (t/is (empty? (cset/difference
                   (-> @env* keys set)
                   #{:results :vars})))))

(t/deftest define-rule-test
  (i/define-rule testrule
    [hello]
    (= hello "hello"))
  (t/testing "Creates var at proper path"
    (t/is (not= nil (get-in @env* [:vars :testrule]))))
  (t/testing "Creates fn"
    (t/is (function? (get-in @env* [:vars :testrule])))))

(t/deftest rule-test
  (i/rule two-is-two
          (= 2 2))
  (let [res (-> @env* :results)]
    (t/testing "calling rule places named entry in results map"
      (t/is (not= nil (:two-is-two res))))
    (t/testing "calling rule creates map in results map"
      (t/is (map? (:two-is-two res))))
    (t/testing "calling rule creates properly-keyed map in results map"
      (let [this-result (:two-is-two res)]
        (t/is (= (-> this-result :rule)
                 :two-is-two))
        (t/is (= (-> this-result :result)
                 :pass))))))

(defn with-new-env
  "Wrap all tests with a call to initialize-eval-env."
  [f]
  (i/initialize-eval-env)
  (f)
  (.unbindRoot #'env*))

(t/use-fixtures :once with-new-env)
