;   Copyright (c) Rich Hickey. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns cljs.cache-test
  (:require [cljs.core.async :refer [chan close! <!]]
            [cljs.test :refer-macros [deftest run-tests testing is are async]]
            [cljs.cache :refer [BasicCache TTLCache LRUCache
                                ttl-cache-factory lru-cache-factory
                                lookup has? hit miss evict seed]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(deftest test-basic-cache-lookup
  (testing "that the BasicCache can lookup as expected"
    (is (= :robot (lookup (miss (BasicCache. {}) '(servo) :robot) '(servo))))))

(defn do-dot-lookup-tests [c]
  (are [expect actual] (= expect actual)
       1   (.lookup c :a)
       2   (.lookup c :b)
       42  (.lookup c :c 42)
       nil (.lookup c :c)))

(defn do-ilookup-tests [c]
  (are [expect actual] (= expect actual)
       1   (:a c)
       2   (:b c)
       42  (:X c 42)
       nil (:X c)))

(defn do-the-assoc [c]
  (are [expect actual] (= expect actual)
       1   (:a (assoc c :a 1))
       nil (:a (assoc c :b 1))))

(defn do-dissoc [c]
  (are [expect actual] (= expect actual)
       2   (:b (dissoc c :a))
       nil (:a (dissoc c :a))
       nil (:b (-> c (dissoc :a) (dissoc :b)))
       0   (count (-> c (dissoc :a) (dissoc :b)))))

(defn do-getting [c]
  (are [actual expect] (= expect actual)
       (get c :a) 1
       (get c :e) nil
       (get c :e 0) 0
       (get c :b 0) 2
       (get c :f 0) nil

       (get-in c [:c :e]) 4
       (get-in c '(:c :e)) 4
       (get-in c [:c :x]) nil
       (get-in c [:f]) nil
       (get-in c [:g]) false
       (get-in c [:h]) nil
       (get-in c []) c
       (get-in c nil) c

       (get-in c [:c :e] 0) 4
       (get-in c '(:c :e) 0) 4
       (get-in c [:c :x] 0) 0
       (get-in c [:b] 0) 2
       (get-in c [:f] 0) nil
       (get-in c [:g] 0) false
       (get-in c [:h] 0) 0
       (get-in c [:x :y] {:y 1}) {:y 1}
       (get-in c [] 0) c
       (get-in c nil 0) c))

(defn do-finding [c]
  (are [expect actual] (= expect actual)
       (find c :a) [:a 1]
       (find c :b) [:b 2]
       (find c :c) nil
       (find c nil) nil))

(defn do-contains [c]
  (are [expect actual] (= expect actual)
       (contains? c :a) true
       (contains? c :b) true
       (contains? c :c) false
       (contains? c nil) false))


(def big-map {:a 1 :b 2 :c {:d 3 :e 4} :f nil :g false nil {:h 5}})
(def small-map {:a 1 :b 2})

(deftest test-basic-cache-ilookup
  (testing "counts"
    (is (= 0 (count (BasicCache. {}))))
    (is (= 1 (count (BasicCache. {:a 1})))))
  (testing "that the BasicCache can lookup via keywords"
    (do-ilookup-tests (BasicCache. small-map)))
  #_(testing "that the BasicCache can .lookup"
    (do-dot-lookup-tests (BasicCache. small-map)))
  (testing "assoc and dissoc for BasicCache"
    (do-the-assoc (BasicCache. {}))
    (do-dissoc (BasicCache. {:a 1 :b 2})))
  (testing "that get and cascading gets work for BasicCache"
    (do-getting (BasicCache. big-map)))
  (testing "that finding works for BasicCache"
    (do-finding (BasicCache. small-map)))
  (testing "that contains? works for BasicCache"
    (do-contains (BasicCache. small-map))))

(defn get-time []
  (.getTime (js/Date.)))

(deftest test-ttl-cache-ilookup
  (let [five-secs (+ 5000 (get-time))
        big-time   (into {} (for [[k _] big-map] [k five-secs]))
        small-time (into {} (for [[k _] small-map] [k five-secs]))]
    (testing "that the TTLCache can lookup via keywords"
      (do-ilookup-tests (TTLCache. small-map small-time 2000)))
    #_(testing "that the TTLCache can lookup via keywords"
      (do-dot-lookup-tests (TTLCache. small-map small-time 2000)))
    (testing "assoc and dissoc for TTLCache"
      (do-the-assoc (TTLCache. {} {} 2000))
      (do-dissoc (TTLCache. {:a 1 :b 2} {:a five-secs :b five-secs} 2000)))
    (testing "that get and cascading gets work for TTLCache"
      (do-getting (TTLCache. big-map big-time 2000)))
    (testing "that finding works for TTLCache"
        (do-finding (TTLCache. small-map small-time 2000)))
    (testing "that contains? works for TTLCache"
        (do-contains (TTLCache. small-map small-time 2000)))))

(deftest test-ttl-cache
  (let [C (ttl-cache-factory {} :ttl 500)]
    (testing "TTL-ness with empty cache"
      (is (= {:a 1 :b 2} (-> C (assoc :a 1) (assoc :b 2) .-cache))))
    (async done
      (let [C1 (-> C (assoc :a 1) (assoc :b 2))
            C2 (-> C (assoc :a 1))]
        (js/setTimeout
         #(do
            (testing "TTL-ness with empty cache, expired"
              (is (= {:c 3} (-> C1 (assoc :c 3) .-cache))))
            (testing "TTL cache does not return a value that has expired"
              (is (nil? (-> C2 (lookup :a)))))
            (done))
         700)))))

(deftest test-lru-cache-ilookup
  (testing "that the LRUCache can lookup via keywords"
    (do-ilookup-tests (LRUCache. small-map {} 0 2)))
  #_(testing "that the LRUCache can lookup via keywords"
    (do-dot-lookup-tests (LRUCache. small-map {} 0 2)))
  (testing "assoc and dissoc for LRUCache"
    (do-the-assoc (LRUCache. {} {} 0 2))
    (do-dissoc (LRUCache. {:a 1 :b 2} {} 0 2)))
  (testing "that get and cascading gets work for LRUCache"
    (do-getting (LRUCache. big-map {} 0 2)))
  (testing "that finding works for LRUCache"
    (do-finding (LRUCache. small-map {} 0 2)))
  (testing "that contains? works for LRUCache"
    (do-contains (LRUCache. small-map {} 0 2))))

(deftest test-lru-cache
  (testing "LRU-ness with empty cache and threshold 2"
    (let [C (lru-cache-factory {} :threshold 2)]
      (are [x y] (= x y)
           {:a 1, :b 2} (-> C (assoc :a 1) (assoc :b 2) .-cache)
           {:b 2, :c 3} (-> C (assoc :a 1) (assoc :b 2) (assoc :c 3) .-cache)
           {:a 1, :c 3} (-> C (assoc :a 1) (assoc :b 2) (hit :a) (assoc :c 3) .-cache))))
  (testing "LRU-ness with seeded cache and threshold 4"
    (let [C (lru-cache-factory {:a 1, :b 2} :threshold 4)]
      (are [x y] (= x y)
           {:a 1, :b 2, :c 3, :d 4} (-> C (assoc :c 3) (assoc :d 4) .-cache)
           {:a 1, :c 3, :d 4, :e 5} (-> C (assoc :c 3) (assoc :d 4) (hit :c) (hit :a) (assoc :e 5) .-cache))))
  (testing "regressions against LRU eviction before threshold met"
    (is (= {:b 3 :a 4}
           (-> (lru-cache-factory {} :threshold 2)
               (assoc :a 1)
               (assoc :b 2)
               (assoc :b 3)
               (assoc :a 4)
               .-cache)))

    (is (= {:e 6, :d 5, :c 4}
           (-> (lru-cache-factory {} :threshold 3)
               (assoc :a 1)
               (assoc :b 2)
               (assoc :b 3)
               (assoc :c 4)
               (assoc :d 5)
               (assoc :e 6)
               .-cache)))

    (is (= {:a 1 :b 3}
           (-> (lru-cache-factory {} :threshold 2)
               (assoc :a 1)
               (assoc :b 2)
               (assoc :b 3)
               .-cache))))

  (is (= {:d 4 :e 5}
         (-> (lru-cache-factory {} :threshold 2)
             (hit :x)
             (hit :y)
             (hit :z)
             (assoc :a 1)
             (assoc :b 2)
             (assoc :c 3)
             (assoc :d 4)
             (assoc :e 5)
             .-cache))))
