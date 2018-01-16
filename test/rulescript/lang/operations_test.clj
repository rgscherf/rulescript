(ns rulescript.lang.operations-test
  (:require [clojure.test :refer :all]
            [rulescript.lang.operations :refer :all]))

(deftest synonyms
  (testing "Application outcomes"
    (is (= warn :warn))
    (is (= fail :fail))
    (is (= pass :pass)))
  (testing "Falsy"
    (is (= not-ok false))
    (is (= no false)))
  (testing "Truthy"
    (is (= ok true))
    (is (= yes true))))

(deftest complete
  (testing "map"
    (is (-> {} complete? not))
    (is (-> {:a nil} complete? not))
    (is (complete? {:a "hello" :b "tester"})))
  (testing "vec"
    (is (-> [] complete? not))
    (is (-> [1 2 {} 4] complete? not))
    (is (-> [1 2 nil 4] complete? not))
    (is (complete? [1 2 3 4])))
  (testing "set"
    (is (-> #{} complete? not))
    (is (complete? #{1 2})))
  (testing "double"
    (is (complete? (Double/POSITIVE_INFINITY)))
    (is (complete? (Double/MAX_VALUE)))
    (is (complete? 0.0))
    (is (complete? 3.33)))
  (testing "boolean"
    (is (complete? false))
    (is (complete? true)))
  (testing "string"
    (is (complete? "anything else"))
    (is (not (complete? ""))))
  (testing "keyword" (is (complete? :haha)))
  (testing "nil" (is (not (complete? nil)))))

(deftest test-in
  (let [data {:a {:aa "Hello" :ab nil :ac [1 2 3]}
              :b nil
              :c {:ca "one"
                  :cb "two"
                  :cc "three"}
              :d {:da [{:daa "one"} {:dab "two"} {:dac "three"}]}
              :e 1}
        sequential [{:a "hi"} {:a "bye"}]]
    (testing "find"
      (is (= nil
             (in data find x y z)))
      (is (= nil
             (in data find b e)))
      (is (= nil
             (in data find b)))
      (is (= "Hello"
             (in data find a aa)))
      (is (= [{:daa "one"} {:dab "two"} {:dac "three"}]
             (in data find d da))))
    (testing "find-each"
      (is (= ["one"]
             (in data find-each (c ca))))
      (is (= ["one" "two"]
             (in data find-each (c ca) (c cb))))
      (is (= [1]
             (in data find-each e)))
      (is (= [nil 1])
          (in data find-each b e)))
    (testing "extract"
      (is (= [nil nil]
             (in sequential extract b)))
      (is (= ["hi" "bye"]
             (in sequential extract a))))
    ))

(deftest test-all-preds
  (testing "not"
    (is (-> nil none? not))
    (is (-> [true false] none? not))
    (is (-> [false false] none?))
    (is (-> [true true] none? not))
    (is (-> [1 2 3] none? not))
    (is (none? [])))
  (testing "all"
    (is (-> nil all? not))
    (is (-> [true false] all? not))
    (is (-> [false false] all? not))
    (is (-> [true true] all?))
    (is (-> [1 2 3] all? not))
    (is (all? []))))