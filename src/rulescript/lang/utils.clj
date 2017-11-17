(ns rulescript.lang.utils)

(defn symbol->keyword
  [symb]
  (-> symb str keyword))

