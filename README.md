[![Clojars Project](https://img.shields.io/clojars/v/rulescript.svg)](https://clojars.org/rulescript) 
[![Build Status](https://travis-ci.org/rgscherf/rulescript.svg?branch=master)](https://travis-ci.org/rgscherf/rulescript)
[![GitHub license](https://img.shields.io/github/license/Naereen/StrapDown.js.svg)](https://github.com/Naereen/StrapDown.js/blob/master/LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)

# RuleScript

If you got here and you're not a software developer, check out the RuleScript website at https://rulescript.org.

RuleScript lets you write policy specifications that are simple, unambiguous, and are applied with utter consistency by a computer. It works by providing a framework for the following equation:

`input documents + validation rules = pass/fail decision`

You write your rules in a simple computer language, point the rules an input document(s), and receive an instant, automatic answer about whether the input conforms to your rules.

## Usage

RuleScript can be used from the command line or as a Clojure library.

### Clojure library (recommended!)

RuleScript is available on Clojars. Add it to your `project.clj` with [![Clojars Project](https://img.shields.io/clojars/v/rulescript.svg)](https://clojars.org/rulescript).

The library provides two functions in `rulescript.core`: 

- `eval-from-files`: takes paths to two files. In both cases, the input file should resolve to one "object", edn and json respectively.
- `eval-from-strings`: takes two strings.

In both cases, the eval fn evaluates an input document against a spec and returns the results either as a clojure map (rule names are keywordized keys) or a pprinted string (if the fn is called with `:pprint true` as a kwarg).

### Command line

You must enable JVM sandboxing in order to use RuleScript on your desktop. It's easy: just copy the following contents to `~/.java.policy`:

```
grant {
  permission java.security.AllPermission;
};
```

Then, clone this repository and run:

`export LEIN_SNAPSHOTS_IN_RELEASE=override; lein uberjar`

Then,

`java -jar rulescript.jar input-spec input-document`

Without file extensions (.edn and .json respectively). Results should be printed to the console.

You may also provide the optional boolean flag `pprint`, as in `pprint false`, to activate/deactivate pretty printing of results. Default is true. A `false` value returns results as a JSON-encoded string.

## Copyright

Copyright © 2017 Robert Scherf. Released under the MIT license.
