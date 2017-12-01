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
     (warn-when
       pass
       (rule is-map-complete?
             (is-complete in)))
     (define-rule remember-complete
                  (app)
                  (is-complete app))
     (rule two-and-two-is-four
           (= (+ 2 2)
              4))
     (apply-rule remember-complete in)
     ))

((eval inspec) inmap)
(def va
  ((eval inspec) inmap))
va

(comment
  (reset! env* {})
  (apply-rule* 'apply-rule-inner 'remember-complete inmap)
  (apply-rule remember-complete inmap)
  (rule is-map-complete?
          (is-complete inmap))
  (warn-when
    :pass
    (rule is-map-complete?
          (is-complete inmap))))
