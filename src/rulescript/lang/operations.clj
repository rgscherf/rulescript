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

;; clojure.core predicate synonyms

(def is-true true?)
(def is-false false?)
(def is true?)
(def is-not false?)
(def equal =)


;; other clojure.core synonyms

(def combine partial)

(def sum +)
(def product *)

;; original predicates

(defn complete?
  [input-map]
  (->> input-map
       (map second)
       (every? #(and (not (= % ""))
                     (not (nil? %))))))

;; combining macros

(defmacro each-of
  [elements existence fn]
  `(->> ~elements
        (map ~fn)
        (every? ~existence)))

(defmacro extract-from-list
  [symb-key lst]
  `(map (symbol->keyword '~symb-key) ~lst))

(defmacro conditional
  "Match test with a number of paired clauses.
  If test matches the first element of a pair, return the second element."
  [on test & clauses]
  `(match ~test ~@clauses))

(defmacro in
  "Retrieve data from a map. Symbols in fields are converted to keyword keys.
  if verb is 'get', dive through the map as get-in.
  if verb is 'get-each', return each argument as if filtering map elements.
  in a get-each, you can dive through the map for certin elements by enclosing them in a list.
  (from data get foo bar) => (get-in data [:foo :bar])
  (from data get-each foo bar) => (map (partial get data) [:foo :bar])
  (from data get-each foo (bar baz) => '((get data :foo), (get-in data [:bar baz]))"
  [data verb & fields]
  `(cond
     (= "find" (str '~verb))
     (get-in ~data
             (mapv symbol->keyword '~fields))
     (= "extract" (str '~verb))
     (map #(get-in %
                   (mapv symbol->keyword '~fields))
          ~data)))

(defn all?
  [bools]
  (every? true? bools))

(defn none?
  [bools]
  (not (all? bools)))


