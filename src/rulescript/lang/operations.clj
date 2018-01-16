(ns rulescript.lang.operations
  (:require [rulescript.lang.utils :refer :all]
            [clojure.core.match :refer [match]]))

;; true/false synonyms

(def yes true)
(def ok true)

(def no false)
(def not-ok false)

(def pass :pass)
(def fail :fail)
(def warn :warn)

;; complete? multimethod

(defmulti complete? (fn [x] (type x)))

(defn- complete-seq [input]
  (and (-> input empty? not)
       (every? complete? input)))
(defn- notnil [in] (not= nil in))

(defmethod complete? nil [_] false)
(defmethod complete? clojure.lang.Keyword [_] true)
(defmethod complete? java.lang.String [input] (not= "" input))
(defmethod complete? java.lang.Boolean [input] (notnil input))
(defmethod complete? java.lang.Double [input] (notnil input))
(defmethod complete? java.lang.Long [input] (notnil input))
(defmethod complete? clojure.lang.APersistentSet [input] (complete-seq input))
(defmethod complete? clojure.lang.APersistentVector [input] (complete-seq input))
(defmethod complete? clojure.lang.ASeq [input] (complete-seq input))
(defmethod complete? clojure.lang.APersistentMap [input]
  (and (-> input empty? not)
       (->> input
            (map second)
            (every? complete?))))

;; combining macros

(defmacro each-of
  [elements existence fn]
  `(->> ~elements
        (map ~fn)
        (every? ~existence)))

(defmacro conditional
  "Match test with a number of paired clauses.
  If test matches the first element of a pair, return the second element."
  [on test & clauses]
  `(match ~test ~@clauses))

(defmacro in
  "Retrieve data from a map. Symbols in fields are converted to keyword keys.
  if verb is 'get', dive through the map as get-in.
  if verb is 'find-each', return each location from the map. a location can be given as a sequence, in which case the sequence acts like a get-in on the map.
  if verb is 'extract', return the same location from each element of a sequence.
  (in data find foo bar)            => (get-in data [:foo :bar])
  (in data find-each (foo bar) baz) => (map #(get-in data %) '([:foo :bar] [:baz]) )
  (in data extract foo bar)         => (map #(get-in % [:foo :bar]) data)"
  [data verb & fields]
  `(cond
     (= "find" (str '~verb))
     (get-in ~data
             (mapv symbol->keyword '~fields))

     (= "find-each" (str '~verb))
     (into []
           (map (partial get-in ~data)
                (map (fn [field-form#]
                       (if (seq? field-form#)
                         (into [] (map symbol->keyword field-form#))
                         [(symbol->keyword field-form#)]))
                     '~fields)))

     (= "extract" (str '~verb))
     (map #(get-in %
                   (mapv symbol->keyword '~fields))
          ~data)))

(defn all?
  [bools]
  (and (-> bools nil? not)
       (every? true? bools)))

(defn none?
  [bools]
  (and (-> bools nil? not)
       (every? false? bools)))
