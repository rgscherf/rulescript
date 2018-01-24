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
  (let [[spec-name input-name & {:strs [pprint timeout] :or {pprint "true"}}] args
        opts {:pprint (boolean
                        (Boolean/valueOf ^String pprint))
              :timeout (try (Integer/valueOf ^String timeout)
                            (catch Exception e nil))
              :printonly true}]
    (use 'rulescript.lang.invocations)
    (use 'rulescript.lang.operations)
    (rulescript-io/eval-from-files opts spec-name input-name)))

(comment
  (eval-from-files "./resources/drao" "./resources/drao" :pprint true :printonly false)
  (-main "./resources/drao" "./resources/drao" "pprint" "true")
  (-main "./resources/drao" "./resources/drao" "pprint" "true" "timeout" "1"))

