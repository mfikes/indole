# Indole

A Clojure(Script) implementation of a leaky bucket rate limiting algorithm.

The idea is that you have a bucket of “entities” that can be used, where the bucket is replenished over a period specified.

This can be useful in situations, say, where you need to rate limit requests to an API that only allows a certain number of requests over a given period.

## Usage

```clojure
(require '[indole.core :refer [make-rate-limiter can-charge?!]])

(def my-rate-limiter (make-rate-limiter 60000 100)) ;; 100 each minute

(when (can-charge?! my-rate-limiter)
  (hit-api))
```

## License

Copyright © 2015 Mike Fikes

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
