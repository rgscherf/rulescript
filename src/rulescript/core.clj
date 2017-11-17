(ns rulescript.core
  (:require [rulescript.io.main :as rulescript-io]
            [rulescript.lang.invocations :refer :all]
            [rulescript.lang.operations :refer :all]
            ))

(def inmap
  {:date "today"
   :city "Victoria"})

(def inspec
  '(validate-document
     (in)
     (rule city-must-be-victoria
           (is-complete in))))

((eval inspec) inmap)

