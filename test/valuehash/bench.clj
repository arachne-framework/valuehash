(ns valuehash.bench
  (:require [valuehash.api :as api]
            [criterium.core :as c]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.random :as random]
            [clojure.test.check.rose-tree :as rose]))

(defn- sample-seq
  "Return a sequence of realized values from `generator`.

  Copy of the built in `sample-seq`, but lets you pass in the seed so benchmark
  runs can be deterministic across machines."
  [generator seed max-size]
  (let [r (random/make-random seed)
        size-seq (gen/make-size-range-seq max-size)]
    (map #(rose/root (gen/call-gen generator %1 %2))
      (gen/lazy-random-states r)
      size-seq)))

(defn- do-bench
  "Benchmark the specified digest function, using the specified seq of sample data"
  [do-digest data]
  (let [data (doall data)]
    (c/with-progress-reporting
      (let [results (c/benchmark
                      (doseq [obj data]
                        (do-digest obj))
                      {})
            hps (/ (count data) (first (:mean results)))]
        (c/report-result results)
        (println "\nThis translates to about" (Math/round hps) "hashed objects per second")))))

(defn bench-small-vectors
  []
  (do-bench api/md5 (take 1000 (sample-seq (gen/vector gen/simple-type-printable) 42 10))))

(defn bench-small-maps
  []
  (do-bench api/md5 (take 1000 (sample-seq (gen/map
                                          gen/simple-type-printable
                                          gen/simple-type-printable)
                              42 10))))

(defn bench-complex
  []
  (do-bench api/md5 (take 1000 (sample-seq gen/any-printable 42 100))))


(comment

  (bench-small-vectors)

  (bench-small-maps)

  (bench-complex)

  )