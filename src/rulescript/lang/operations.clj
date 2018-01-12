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

(defn- notnil [in] (not= nil in))
;; complete? multimethod
(defmulti complete? (fn [x] (type x)))
(defmethod complete? nil [_] false)
(defmethod complete? clojure.lang.Keyword [_] true)
(defmethod complete? java.lang.String [input] (not= "" input))
(defmethod complete? java.lang.Boolean [input] (notnil input))
(defmethod complete? java.lang.Double [input] (notnil input))
(defmethod complete? java.lang.Long [input] (notnil input))
(defmethod complete? clojure.lang.PersistentHashMap
  [input]
  (->> input
       (map second)
       (every? complete?)))
(defmethod complete? clojure.lang.PersistentArrayMap
  [input]
  (->> input
       (map second)
       (every? complete?)))
(defmethod complete? clojure.lang.PersistentVector
  [input]
  (every? complete? input))

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
  (from data get foo bar)          => (get-in data [:foo :bar])
  (from data get-in (foo bar) baz) => (map #(get-in data %) '([:foo :bar] [:baz]) )
  (from data extract foo bar)      => (map #(get-in % [:foo :bar]) data)"
  [data verb & fields]
  `(cond
     (= "find" (str '~verb))
     (get-in ~data
             (mapv symbol->keyword '~fields))

     (= "find-each" (str '~verb))
     (map (fn [element#]
            (get-in ~data element#))
          (map (fn [field-form#]
                 (if (seq? field-form#)
                   (into [] (map symbol->keyword field-form#))
                   [(symbol->keyword field-form#)]))
               '~fields))

     (= "extract" (str '~verb))
     (map #(get-in %
                   (mapv symbol->keyword '~fields))
          ~data)))

(defn all?
  [bools]
  (every? true? bools))

(defn none?
  [bools]
  (every? false? bools))


