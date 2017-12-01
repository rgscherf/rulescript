# RuleScript

RuleScript lets you write policy specifications that are simple, unambiguous, and are applied with utter consistency by a computer. It works by providing a framework for the following equation:

`input documents + validation rules = pass/fail decision`

You write your rules in a simple computer language, point the rules an input document(s), and receive an instant, automatic answer about whether the input conforms to your rules.

RuleScript is fast and flexible enough to save time on a one-week project. It's also built on industrial-strength software that can scale to a team of hundreds.

## Useage

Clone this repository and run `lein uberjar`. Then, 

`java -jar rulescript.jar input-spec input-document`

Results should be printed to the console.

Coming soon: evaluation on the web using ClojureScript.

## License

Copyright © 2017 Robert Scherf

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
