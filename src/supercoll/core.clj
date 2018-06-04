(ns supercoll.core
  (:refer-clojure :exclude [count filter first map range reduce seq])
  (:import [java.util Optional]
           [java.util.function Consumer Predicate UnaryOperator BinaryOperator]
           [java.util.stream Stream]))

;; Java Functional API
;; ===================
(defn consumer [f]
  (reify Consumer
    (accept [_ v]
      (f v))))

(defn predicate [f]
  (reify Predicate
    (test [_ v]
      (f v))))

(defn unary-op [f]
  (reify UnaryOperator
    (apply [_ v]
      (f v))))

(defn binary-op [f]
  (reify BinaryOperator
    (apply [_ x y]
      (f x y))))

(defn <-optional
  ([^Optional o]
   (<-optional o nil))
  ([^Optional o default]
   (if (.isPresent o)
     (.get o)
     default)))


;; Stream API
;; ==========
(defn stream [coll]
  (if (instance? Stream coll)
    coll
    (.stream coll)))

(defn seq [^Stream s]
  (-> s .iterator iterator-seq))

(defn ^Stream range
  ([end-exclusive]
   (range 0 end-exclusive 1))
  ([start-inclusive end-exclusive]
   (range start-inclusive end-exclusive 1))
  ([start-inclusive end-exclusive step]
   (stream (clojure.core/range start-inclusive end-exclusive step))))

(defn first [coll]
  (-> (stream coll)
      .findFirst
      <-optional))

(defn count [coll]
  (.count (stream coll)))

(defn for-each
  ([coll f]
   (for-each coll f false))
  ([coll f ordered?]
   (let [s (stream coll)
         c (consumer f)]
     (if ordered?
       (.forEachOrdered s c)
       (.forEach s c)))))

(defn map
  [coll f]
  (.map (stream coll)
        (unary-op f)))

(defn filter
  [coll f]
  (.filter (stream coll)
           (predicate f)))

(defn reduce
  ([coll f]
   (-> (stream coll)
       (.reduce (binary-op f))
       <-optional))
  ([coll f x]
   (.reduce (stream coll)
            x
            (binary-op f))))
