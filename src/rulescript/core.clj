(ns rulescript.core
  (:require [rulescript.io.main :as rulescript-io]
            [rulescript.lang.invocations :refer :all]
            [rulescript.lang.operations :refer :all]
            ))

(defn -main
  [spec-name input-name & others]
  (rulescript-io/eval-with spec-name input-name))
