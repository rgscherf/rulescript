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
     (rule is-map-complete?
           (is-complete in))
     (define-rule remember-complete
                  (app)
                  (is-complete app))

     (apply-rule remember-complete in)
     ))

(def va
  ((eval inspec) inmap))
va
