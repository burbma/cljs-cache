(ns cache.macros
  (:use [cljs.core]))

#_(defmacro debug [expr]
      `(let [result# ~expr]
         (println "Evaluating:" '~expr)
         (println "Result:" result#)
         result#))

#_(defmacro defcache
  [record-name fields & specifics]
  (let [[base-field & _] fields]
    `(defrecord ~record-name [~@fields]
       ~@specifics
       ILookup
       (-lookup [this# key#]
         (println "here")
         (lookup this# key#))
       (-lookup [this# key# not-found#]
         (if (has? this# key#)
           (lookup this# key#)
           not-found#)))))

#_(defmacro defcache
  [type-name fields & specifics]
  (let [[base-field & _] fields]
    `(deftype ~type-name [~@fields]
       ~@specifics
       ILookup
       (-lookup [this# key#]
         (lookup this# key#)))))
