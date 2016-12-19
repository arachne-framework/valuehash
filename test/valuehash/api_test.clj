(ns valuehash.api-test
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [valuehash.api :as api]
            ))

(defprotocol Perturbable
  "A value that can be converted to a value of a different type, but stil be equal"
  (perturb [obj] "Convert an object to a different but equal object"))

(defn select
  "Deterministically select one of the options (based on the hash of the key)"
  [key & options]
  (nth options (mod (hash key) (count options))))

(extend-protocol Perturbable

  Object
  (perturb [obj] obj)

  Long
  (perturb [l]
    (select l
      (if (< Byte/MIN_VALUE l Byte/MAX_VALUE) (byte l) l)
      (if (< Integer/MIN_VALUE l Integer/MAX_VALUE) (int l) l)))

  Double
  (perturb [d]
    (if (= d (unchecked-float d))
      (unchecked-float d)
      d))

  java.util.Map
  (perturb [obj]
    (let [keyvals (interleave (reverse (keys obj))
                              (reverse (map perturb (vals obj))))]
      (select obj
        (apply array-map keyvals)
        (apply hash-map keyvals)
        (java.util.HashMap. (apply array-map keyvals)))))

  java.util.List
  (perturb [obj]
    (let [l (map perturb obj)]
       (select obj
         (lazy-seq l)
         (apply vector l)
         (apply list l)
         (java.util.ArrayList. l)
         (java.util.LinkedList. l))))

  java.util.Set
  (perturb [obj]
    (let [s (reverse (map perturb obj))]
      (select obj
        (apply hash-set s)
        (java.util.HashSet. s)
        (java.util.LinkedHashSet. s)))))


(defspec value-semantics-hold 150
  (prop/for-all [o gen/any-printable]
    (let [p (perturb o)]
      (= (api/md5-str o) (api/md5-str p))
      (= (api/sha-1-str o) (api/sha-1-str p))
      (= (api/sha-256-str o) (api/sha-256-str p)))))
