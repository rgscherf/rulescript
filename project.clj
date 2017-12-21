(defproject rulescript "0.2.2"
  :description "Comprehensive DSL for document validation."
  :url "http://rulescript.org"
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}
  :main rulescript.core
  :aot [rulescript.core]
  :dependencies [
                 [org.clojure/clojure "1.9.0"]
                 [cheshire "5.8.0"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 ])
