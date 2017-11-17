(ns rulescript.core
  (:require [rulescript.io.main :as rulescript-io]
            ))

(def inmap
  {:date "today"
   :city "Victoria"})

(def inspec
  '(validate-document
     (in)
     (rule city-must-be-victoria
           )))

