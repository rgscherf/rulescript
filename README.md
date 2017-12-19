# RuleScript

If you got here and you're not a software developer, check out the RuleScript website at https://rulescript.org.

RuleScript lets you write policy specifications that are simple, unambiguous, and are applied with utter consistency by a computer. It works by providing a framework for the following equation:

`input documents + validation rules = pass/fail decision`

You write your rules in a simple computer language, point the rules an input document(s), and receive an instant, automatic answer about whether the input conforms to your rules.

## Useage

RuleScript can be used from the command line or as a Clojure library.

### Clojure library (recommended!)

RuleScript is available on Clojars. Add it to your `project.clj` with `[rulescript "0.1.0]`.

The library provides two functions in `rulescript.core`: 

- `eval-from-files`: takes paths to two file objects.
- `eval-from-strings`: takes two strings.

In both cases, the eval fn evaluates an input document against a spec and returns the results either as a clojure map (rule names are keywordized keys) or a pprinted string (if the fn is called with `:pprint true`).

### Command line

Clone this repository and run `lein uberjar`. Then, 

`java -jar rulescript.jar input-spec input-document :pprint true`

Results should be printed to the console.

## Copyright

Copyright © 2017 Robert Scherf. Released under the MIT license.
