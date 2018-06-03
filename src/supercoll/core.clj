(ns supercoll.core
  (:refer-clojure :exclude [butlast count get list nth persistent! rest set transient])
  (:import [io.lacuna.bifurcan IForkable ILinearizable IList IMap List Map]))

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
  [^IList lst from till]
  (.slice lst from till))


;; Util
;; ====
(letfn [(count-list [^IList l]
          (.size l))
        (count-dict [^IMap m]
          (.size m))]
  (defn count
    [scoll]
    (condp instance? scoll
      IList (count-list scoll)
      IMap  (count-dict scoll))))

(letfn [(transient-list? [^IList l]
          (.isLinear l))
        (transient-dict? [^IMap m]
          (.isLinear m))]
  (defn transient?
    [^ILinearizable scoll]
    (condp instance? scoll
      IList (transient-list? scoll)
      IMap  (transient-dict? scoll))))

(defn ^IList transient
  [^ILinearizable coll]
  (.linear coll))

(defn ^IList persistent!
  [^IForkable coll]
  (.forked coll))
