(ns rulescript.io.main
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [cheshire.core :refer [parse-stream]])
  (:import (java.io PushbackReader)))

(defn reader-from-str
  [filename extension]
  (let [xt (if (clojure.string/starts-with? extension ".")
             extension
             (str "." extension))]
    (-> (#(str "poli/" filename xt))
        io/resource
        io/reader)))

(defn- get-document
  "Read a JSON document by its name (excluding file extension)."
  ;; TODO graceful error handling when the file is not found.
  [doc-name]
  (parse-stream (reader-from-str doc-name "json")
                true))

(defn- get-polispec
  "Read a named spec file (without file extension) into memory. The file still needs to be eval'd."
  ;; TODO graceful error handling when the file is not found.
  [spec-name]
  (let [spec-resource (reader-from-str spec-name "edn")]
    (with-open [in (PushbackReader. spec-resource)]
      (edn/read {:eof :eof} in))))

(defn eval-with
  [specname inputname]
  (let [input (get-document inputname)
        spec (get-polispec specname)]
    ((eval spec) input)))
