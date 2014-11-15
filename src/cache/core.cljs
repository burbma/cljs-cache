;   Copyright (c) Rich Hickey. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns ^{:doc "A port of clojure/core.cache to Clojurescript"
      :author "Timothy Galebach"}
  cache.core
  (:require [weasel.repl :as ws-repl]))

(enable-console-print!)
(ws-repl/connect "ws://localhost:9002")

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
  "The basic hit/miss logic for the cache system.  Expects a wrap function and
  value function.  The wrap function takes the value function and the item in question
  and is expected to run the value function with the item whenever a cache
  miss occurs.  The intent is to hide any cache-specific cells from leaking
  into the cache logic itelf."
  ([cache item] (through default-wrapper-fn identity cache item))
  ([value-fn cache item] (through default-wrapper-fn value-fn cache item))
  ([wrap-fn value-fn cache item]
    (if (has? cache item)
      (hit cache item)
      (miss cache item (wrap-fn #(value-fn %) item)))))

(defn- get-time []
  (.getTime (js/Date.)))

(defn- key-killer-fn
  "returns a fn that dissocs expired keys from a map"
  [ttl expiry now]
  (let [ks (map key (filter #(> (- now (val %)) expiry) ttl))]
    #(apply dissoc % ks)))

(deftype TTLCache [cache ttl ttl-ms]
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


  ILookup
  (-lookup [this item]
    (lookup this item))
  (-lookup [this key not-found]
    (if (has? this key)
      (lookup this key)
      not-found))
  Object
  (toString [_]
    (str cache \, \space ttl \, \space ttl-ms)))

(deftype BasicCache [cache]
  CacheProtocol
  (lookup [_ item]
    (get cache item))
  (lookup [_ item not-found]
    (get cache item not-found))
  (seed [_ base]
    (BasicCache. base))
  (has? [_ item]
    (contains? cache item))
  (hit [this item] this)
  (miss [_ item result]
    (BasicCache. (assoc cache item result)))
  (evict [_ key]
    (BasicCache. (dissoc cache key)))

  ILookup
  (-lookup [this item]
    (lookup this item))
  (-lookup [this key not-found]
    (if (has? this key)
      (lookup this key)
      not-found))
  Object
  (toString [_] (str cache)))

(defn ttl-cache-factory
  "Returns a TTL cache with the cache and expiration-table initialied to `base` --
   each with the same time-to-live.

   This function also allows an optional `:ttl` argument that defines the default
   time in milliseconds that entries are allowed to reside in the cache."
  [base & {ttl :ttl :or {ttl 2000}}]
  {:pre [(number? ttl) (<= 0 ttl)
         (map? base)]}
  (seed (TTLCache. {} {} ttl) base))
