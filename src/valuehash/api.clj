(ns valuehash.api
  (:require [valuehash.impl :as impl]
            [valuehash.specs])
  (:import [java.security MessageDigest]
           [java.io InputStream ByteArrayInputStream]))

(defn digest
  "Given a digest function and an arbitrary Clojure object, return a byte array
  representing the digest of the object.

  The digest function must take an InputStream as its argument, and return a
  byte array."
  ^bytes [digest-fn obj]
  (digest-fn (ByteArrayInputStream. (impl/to-byte-array obj))))

(defn- consume
  "Fully consume the specified input stream, using the supplied MessageDigest
  object."
  [^MessageDigest digest ^InputStream is]
  (let [buf (byte-array 64)]
    (loop []
      (let [read (.read is buf)]
        (when (<= 0 read)
          (.update digest buf 0 read)
          (recur))))
    (.digest digest)))

(defn messagedigest-fn
  "Return a digest function using java.security.MessageDigest, using the specified algorithm"
  [algorithm]
  (fn [is]
    (consume (MessageDigest/getInstance algorithm) is)))

(defn hex-str
  "Return the hexadecimal string representation of a byte array"
  [ba]
  (apply str (map #(format "%02x" %) ba)))

(defn md5
  "Return the MD5 digest of an arbitrary Clojure data structure"
  [obj]
  (digest (messagedigest-fn "MD5") obj))

(defn md5-str
  "Return the MD5 digest of an arbitrary Clojure data structure, as a string"
  [obj]
  (hex-str (md5 obj)))

(defn sha-1
  "Return the SHA-1 digest of an arbitrary Clojure data structure"
  [obj]
  (digest (messagedigest-fn "SHA-1") obj))

(defn sha-1-str
  "Return the SHA-1 digest of an arbitrary Clojure data structure, as a string"
  [obj]
  (hex-str (sha-1 obj)))

(defn sha-256
  "Return the SHA-256 digest of an arbitrary Clojure data structure"
  [obj]
  (digest (messagedigest-fn "SHA-256") obj))

(defn sha-256-str
  "Return the sha256 digest of an arbitrary Clojure data structure, as a string"
  [obj]
  (hex-str (sha-256 obj)))