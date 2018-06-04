(ns supercoll.persistent
  (:refer-clojure :exclude [get remove update list nth last rest butlast set transient persistent!])
  (:import [io.lacuna.bifurcan IList IMap ILinearizable IForkable List Map]))

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

(defn last
  [^IList lst]
  (.last lst))

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
