(ns supercoll.core
  (:refer-clojure :exclude [butlast count get list map nth persistent! range rest remove set transient update])
  (:import [io.lacuna.bifurcan IForkable ILinearizable IList IMap List Map Maps$Entry]
           [java.util.function Consumer Predicate UnaryOperator DoubleUnaryOperator LongUnaryOperator]
           [java.util.stream BaseStream LongStream]))

;; Hashmap
;; =======
(defn ^IMap dict
  [& kvs]
  (Map/from ^java.util.Map (apply hash-map kvs)))

(defn get
  ([^IMap d k]
   (get d k nil))
  ([^IMap d k default]
   (.get d k default)))

(defn ^IMap put
  [^IMap d k v]
  (.put d k v))

(defn ^IMap remove
  [^IMap d k]
  (.remove d k))

(defn ^IMap update
  [^IMap d k f]
  (put d k (f (get d k))))


;; List
;; ====
(defn ^IList list
  ([]
   (List.))
  ([& items]
   (List/from ^java.util.List items)))

(defn nth
  [^IList lst n]
  (.nth lst (long n)))

(defn ^IList add-first
  [^IList l v]
  (.addFirst l v))

(defn ^IList add-last
  [^IList l v]
  (.addLast l v))

(defn ^IList rest
  [^IList lst]
  (.removeFirst lst))

(defn ^IList butlast
  [^IList lst]
  (.removeLast lst))

(defn ^IList set
  [^IList lst idx val]
  (.set lst idx val))

(defn ^IList slice
  [^IList lst start-inclusive end-exclusive]
  (.slice lst start-inclusive end-exclusive))


;; Util
;; ====
(defn count
  [scoll]
  (condp instance? scoll
    IList (.size ^IList scoll)
    IMap  (.size ^IMap scoll)))

(defn transient?
  [^ILinearizable scoll]
  (condp instance? scoll
    IList (.isLinear ^IList scoll)
    IMap  (.isLinear ^IMap scoll)))

(defn ^IList transient
  [^ILinearizable coll]
  (.linear coll))

(defn ^IList persistent!
  [^IForkable coll]
  (.forked coll))

(defn ->seq [scoll-or-stream]
  (condp instance? scoll-or-stream
    IList (seq scoll-or-stream)
    IMap  (for [^Maps$Entry entry (seq scoll-or-stream)]
            [(.key entry) (.value entry)])
    BaseStream (-> ^BaseStream
                   scoll-or-stream
                   .iterator
                   iterator-seq)))


;; Java Functional API
;; ===================
(defn consumer [f]
  (reify Consumer
    (accept [_ v]
      (f v))))

(defn unary-op [f]
  (reify UnaryOperator
    (apply [_ v]
      (f v))))

(defn predicate [f]
  (reify Predicate
    (test [_ v]
      (f v))))


;; Stream API
;; ==========
(defn stream
  [coll]
  (condp instance? coll
    IList (.stream ^IList coll)
    IMap  (.stream ^IMap coll)
    BaseStream coll
    ;; else
    (.stream coll)))

(defn ^Stream range
  ([end-exclusive]
   (range 0 end-exclusive 1))
  ([start-inclusive end-exclusive]
   (range start-inclusive end-exclusive 1))
  ([start-inclusive end-exclusive step]
   (->> (clojure.core/range start-inclusive end-exclusive step)
        (apply list)
        stream)))

(defn for-each
  [scoll f]
  (.forEach (stream scoll)
            (consumer f)))

(defn map
  [s f]
  (.map (stream s)
        (unary-op f)))


;; READERS
;; -------
(defn read-list [coll]
  (apply list coll))

(defn read-dict [m]
  (apply dict (flatten (seq m))))

(set! *data-readers*
      (assoc *data-readers*
             'list #'read-list
             'dict #'read-dict))
