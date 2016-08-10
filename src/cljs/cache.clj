;   Copyright (c) Rich Hickey. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns cljs.cache)

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
