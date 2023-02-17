;   Copyright (c) Rich Hickey. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns ^{:doc "A port of clojure/core.cache to Clojurescript"
      :author "Matt Burbidge"}
    cljs.cache
  (:require [tailrecursion.priority-map :refer [priority-map]])
  (:require-macros [cljs.cache :refer [defcache]]))

(defprotocol CacheProtocol
  "This is the protocol describing the basic cache capability."
  (lookup [cache e]
    [cache e not-found]
    "Retrieve the value associated with `e` if it exists, else `nil` in
  the 2-arg case. Retrieve the value associated with `e` if it exists,
  else `not-found` in the 3-arg case.")
  (has? [cache e]
    "Checks if the cache contains a value associated with `e`")
  (hit [cache e]
    "Is meant to be called if the cache is determined to contain a value
  associated with `e`")
  (miss [cache e ret]
    "Is meant to be called if the cache is determined to **not** contain a
  value associated with `e`")
  (evict [cache e]
    "Removes an entry from the cache")
  (seed [cache base]
    "Is used to signal that the cache should be created with a seed.
  The contract is that said cache should return an instance of its
  own type."))

(def ^{:private true} default-wrapper-fn #(%1 %2))

(defn through
  "The basic hit/miss logic for the cache system. Expects a wrap function and
  value function.  The wrap function takes the value function and the item in
  question and is expected to run the value function with the item whenever a
  cache miss occurs.  The intent is to hide any cache-specific cells from
  leaking into the cache logic itelf."
  ([cache item] (through default-wrapper-fn identity cache item))
  ([value-fn cache item] (through default-wrapper-fn value-fn cache item))
  ([wrap-fn value-fn cache item]
   (if (has? cache item)
     (hit cache item)
     (miss cache item (wrap-fn #(value-fn %) item)))))

(defn through-cache
  "The basic hit/miss logic for the cache system.  Like through but always has
  the cache argument in the first position for easier use with swap! etc."
  ([cache item] (through-cache cache item default-wrapper-fn identity))
  ([cache item value-fn] (through-cache cache item default-wrapper-fn value-fn))
  ([cache item wrap-fn value-fn]
   (if (has? cache item)
     (hit cache item)
     (miss cache item (wrap-fn #(value-fn %) item)))))

(defcache BasicCache [cache]
  CacheProtocol
  (lookup [_ item]
    (get cache item))
  (lookup [_ item not-found]
    (get cache item not-found))
  (has? [_ item]
    (contains? cache item))
  (hit [this item] this)
  (miss [_ item result]
    (BasicCache. (assoc cache item result)))
  (evict [_ key]
    (BasicCache. (dissoc cache key)))
  (seed [_ base]
    (BasicCache. base))
  Object
  (toString [_] (str cache)))

;; TTL Cache

(defn- get-time []
  (.getTime (js/Date.)))

(defn- key-killer-fn
  "returns a fn that dissocs expired keys from a map"
  [ttl expiry now]
  (let [ks (map key (filter #(> (- now (val %)) expiry) ttl))]
    #(apply dissoc % ks)))

(defcache TTLCache [cache ttl ttl-ms]
  CacheProtocol
  (lookup [this item]
    (let [ret (lookup this item ::nope)]
      (when-not (= ret ::nope) ret)))
  (lookup [this item not-found]
    (if (has? this item)
      (get cache item)
      not-found))
  (has? [_ item]
    (let [t (get ttl item (- ttl-ms))]
      (< (- (get-time)
            t)
         ttl-ms)))
  (hit [this item] this)
  (miss [this item result]
    (let [now  (get-time)
          kill-old (key-killer-fn ttl ttl-ms now)]
      (TTLCache. (assoc (kill-old cache) item result)
                 (assoc (kill-old ttl) item now)
                 ttl-ms)))
  (seed [_ base]
    (let [now (get-time)]
      (TTLCache. base
                 (into {} (for [x base] [(key x) now]))
                 ttl-ms)))
  (evict [_ key]
    (TTLCache. (dissoc cache key)
               (dissoc ttl key)
               ttl-ms))
  Object
  (toString [_]
    (str cache \, \space ttl \, \space ttl-ms)))

;; LRU Cache

(defn- build-leastness-queue
  [base limit start-at]
  (into (priority-map)
        (concat (take (- limit (count base)) (for [k (range (- limit) 0)] [k k]))
                (for [[k _] base] [k start-at]))))


(defcache LRUCache [cache lru tick limit]
  CacheProtocol
  (lookup [_ item]
    (get cache item))
  (lookup [_ item not-found]
    (get cache item not-found))
  (has? [_ item]
    (contains? cache item))
  (hit [_ item]
    (let [tick+ (inc tick)]
      (LRUCache. cache
                 (if (contains? cache item)
                   (assoc lru item tick+)
                   lru)
                 tick+
                 limit)))
  (miss [_ item result]
    (let [tick+ (inc tick)]
      (if (>= (count lru) limit)
        (let [k (if (contains? lru item)
                  item
                  (first (peek lru))) ;; minimum-key, maybe evict case
              c (-> cache (dissoc k) (assoc item result))
              l (-> lru (dissoc k) (assoc item tick+))]
          (LRUCache. c l tick+ limit))
        (LRUCache. (assoc cache item result)  ;; no change case
                   (assoc lru item tick+)
                   tick+
                   limit))))
  (evict [this key]
    (if (contains? cache key)
      (LRUCache. (dissoc cache key)
                 (dissoc lru key)
                 (inc tick)
                 limit)
      this))
  (seed [_ base]
    (LRUCache. base
               (build-leastness-queue base limit 0)
               0
               limit))
  Object
  (toString [_]
    (str cache \, \space lru \, \space tick \, \space limit)))


;; Factories

(defn basic-cache-factory
  "Returns a pluggable basic cache initialied to `base`"
  [base]
  {:pre [(map? base)]}
  (BasicCache. base))

(defn ttl-cache-factory
  "Returns a TTL cache with the cache and expiration-table initialied to `base` --
   each with the same time-to-live.

   This function also allows an optional `:ttl` argument that defines the default
   time in milliseconds that entries are allowed to reside in the cache."
  [base & {ttl :ttl :or {ttl 2000}}]
  {:pre [(number? ttl) (<= 0 ttl)
         (map? base)]}
  (seed (TTLCache. {} {} ttl) base))

(defn lru-cache-factory
  "Returns an LRU cache with the cache and usage-table initialied to `base` --
   each entry is initialized with the same usage value.
   This function takes an optional `:threshold` argument that defines the maximum number
   of elements in the cache before the LRU semantics apply (default is 32)."
  [base & {threshold :threshold :or {threshold 32}}]
  {:pre [(number? threshold) (< 0 threshold)
         (map? base)]}
  (seed (LRUCache. {} (priority-map) 0 threshold) base))
