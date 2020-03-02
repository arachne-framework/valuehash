(ns valuehash.impl
  "Simple implementation based on plain byte arrays"
  (:import [java.util UUID Date]
           [java.math BigDecimal BigInteger]))

(defprotocol CanonicalByteArray
  "An object that can be converted to a canonical byte array, with value
  semantics intact (that is, two objects that are clojure.core/= will always
  have the same binary representation)"
  (to-byte-array [this] "Convert an object to a canonical byte array"))

(defn- ba-comparator
  "Comparator function for byte arrays"
  ^long [^bytes a ^bytes b]
  (let [alen (alength a)
        blen (alength b)]
    (if (not= alen blen)
      (- alen blen)
      ; compare backward, since lots of symbols/keywords have a common prefix
      (loop [i (dec alen)]
        (if (< i 0)
          0
          (let [c (- (aget a i) (aget b i))]
            (if (= 0 c)
              (recur (dec i))
              c)))))))

(defn long->bytes
  "Convert a long value to a byte array"
  [val]
  (.toByteArray (biginteger val)))

(defn- join-byte-arrays
  "Copy multiple byte arrays to a single output byte array in the order they
  are given."
  [arrays]
  (let [dest (byte-array (+ (reduce + (map alength arrays))))]
    (loop [offset 0
           [^bytes src & more] arrays]
      (when src
        (let [srclen (alength src)]
          (System/arraycopy src 0 dest offset srclen)
          (recur (+ offset srclen) more))))
    dest))

;; Primitive values
(extend-protocol CanonicalByteArray
  nil
  (to-byte-array [_] (byte-array 1 (byte 0)))
  String
  (to-byte-array [this] (.getBytes ^String this))
  clojure.lang.Keyword
  (to-byte-array [this] (.getBytes (str this)))
  clojure.lang.Symbol
  (to-byte-array [this] (.getBytes (str this)))
  Byte
  (to-byte-array [this] (long->bytes this))
  Integer
  (to-byte-array [this] (long->bytes this))
  Long
  (to-byte-array [this] (long->bytes this))
  Double
  (to-byte-array [this] (long->bytes (Double/doubleToLongBits this)))
  Float
  (to-byte-array [this] (long->bytes (Double/doubleToLongBits this)))
  clojure.lang.Ratio
  (to-byte-array [this] (long->bytes (Double/doubleToLongBits (double this))))
  clojure.lang.BigInt
  (to-byte-array [this] (.toByteArray (.toBigInteger this)))
  BigInteger
  (to-byte-array [this] (.toByteArray this))
  BigDecimal
  (to-byte-array [this] (long->bytes (Double/doubleToLongBits (double this))))
  Boolean
  (to-byte-array [this] (byte-array 1 (if this (byte 1) (byte 0))))
  Character
  (to-byte-array [this] (.getBytes (str this)))
  UUID
  (to-byte-array [this]
    (join-byte-arrays [(long->bytes (.getMostSignificantBits ^UUID this))
                       (long->bytes (.getLeastSignificantBits ^UUID this))]))
  Date
  (to-byte-array [this]
    (long->bytes (.getTime this))))

(def list-sep (byte-array 1 (byte 42)))
(def set-sep (byte-array 1 (byte 21)))
(def map-sep (byte-array 1 (byte 19)))

(defn- map-entry->byte-array
  [map-entry]
  (join-byte-arrays [(to-byte-array (.getKey map-entry))
                     (to-byte-array (.getValue map-entry))]))

;; Collections
(extend-protocol CanonicalByteArray
  java.util.List
  (to-byte-array [this]
    (->> this
      (map to-byte-array)
      (interpose list-sep)
      (cons list-sep)
      (join-byte-arrays)))
  java.util.Set
  (to-byte-array [this]
    (->> this
      (map to-byte-array)
      (sort ba-comparator)
      (interpose set-sep)
      (cons set-sep)
      (join-byte-arrays)))
  java.util.Map
  (to-byte-array [this]
    (->> this
      (map map-entry->byte-array)
      (sort ba-comparator)
      (cons map-sep)
      (join-byte-arrays))))
