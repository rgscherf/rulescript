(ns rulescript.core
  (:require [rulescript.io.main :as rulescript-io]
            [rulescript.lang.invocations :refer :all]
            [rulescript.lang.operations :refer :all]
            )
  (:gen-class))

(def eval-from-strings rulescript-io/eval-from-strings)
(def eval-from-files rulescript-io/eval-from-files)

(defn -main
  [& args]
  (let [[spec-name input-name & {:strs [pprint] :or {pprint true}}] args
        pprintval (boolean (Boolean/valueOf ^String pprint))]
    (use 'rulescript.lang.invocations)
    (use 'rulescript.lang.operations)
    (rulescript-io/eval-from-files spec-name input-name :pprint pprintval)))

(comment
  (eval-from-files "./resources/drao" "./resources/drao" :pprint false)
  (-main "./resources/drao" "./resources/drao" "pprint" "true")
  (-main "./resources/drao" "./resources/drao" "pprint" "false"))

