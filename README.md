# identihash

A Clojure library that provides a way to provide higher-bit hashes of arbitrary
Clojure data structures, which respect Clojure's identity semantics. That is, if
two objects are `clojure.core/=`, they will have the same hash value. To my
knowledge, no other Clojure data hashing libraries make this guarantee.

The protocol is extensible to arbitrary data types and can work with any hash
function that can take a byte array.

## Usage

TODO

## License

Copyright © 2016 Luke VanderHart

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

