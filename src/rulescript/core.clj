(ns rulescript.core
  (:require [rulescript.io.main :as rulescript-io]
            [rulescript.lang.invocations :refer :all]
            [rulescript.lang.operations :refer :all]
            )
  (:gen-class))

(def eval-from-strings rulescript-io/eval-from-strings)
(def eval-from-files rulescript-io/eval-from-files)

(defn -main
  [spec-name input-name & others]
  (eval-from-files spec-name input-name))

(comment
  (eval-from-files "./resources/drao" "./resources/drao" :pprint true))


