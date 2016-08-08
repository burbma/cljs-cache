(ns cache.core)

(defmacro defcache
  [type-name fields & specifics]
  (let [[base & _] fields
        base-field (with-meta base {:tag 'cljs.core/IMap})]
    `(deftype ~type-name [~@fields]
       ~@specifics

       cljs.core/ILookup
       (~'-lookup [this# key#]
         (~'-lookup this# key# nil))
       (~'-lookup [this# key# not-found#]
         (if (has? this# key#)
           (lookup this# key#)
           not-found#))

       cljs.core/IIterable
       (~'-iterator [_#]
        (.iterator ~base-field))

       cljs.core/IAssociative
       (~'-assoc [this# k# v#]
        (miss this# k# v#))
       (~'-contains-key? [this# k#]
        (has? this# k#))

       cljs.core/IMap
       (~'-dissoc [this# k#]
        (evict this# k#))

       cljs.core/ICounted
       (~'-count [this#]
        (~'-count ~base-field))

       cljs.core/ICollection
       (~'-conj [this# elem#]
        (seed this# ('-conj ~base-field elem#)))

       cljs.core/IEquiv
       (~'-equiv [this# other#]
        (= other# ~base-field))

       cljs.core/IEmptyableCollection
       (~'-empty [this#]
        (seed this# ('-empty ~base-field)))

       cljs.core/ISeqable
       (~'-seq [_#]
        (~'-seq ~base-field)))))
