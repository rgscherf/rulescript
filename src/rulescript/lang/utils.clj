(ns rulescript.lang.utils
  (:require [clojure.string :as string]))

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

