(ns rulescript.lang.utils
  (:require [clojure.string :as string])
  (:import (java.io IOException)))

(defn symbol->keyword
  [symb]
  (-> symb str keyword))

(defn stringify-fn
  "Render a fn's name as a string."
  [name]
  (->>
    (string/split name #"-")
    (map string/capitalize)
    (string/join " ")
    string/trim))

(defn merge-ignore-nil
  [& maps]
  (apply merge-with
         (fn [a b]
           (if (nil? b)
             a
             b))
         maps))

(defn timeout-exception
  [timeout-val]
  (IOException.
    (str "Your specification took too long to evaluate (> "
         (double (/ timeout-val 1000))
         " seconds)")))

