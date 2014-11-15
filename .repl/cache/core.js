// Compiled by ClojureScript 0.0-2311
goog.provide('cache.core');
goog.require('cljs.core');
goog.require('weasel.repl');
goog.require('weasel.repl');
cljs.core.enable_console_print_BANG_.call(null);
weasel.repl.connect.call(null,"ws://localhost:9002");
cache.core.CacheProtocol = (function (){var obj11947 = {};return obj11947;
})();
cache.core.lookup = (function() {
var lookup = null;
var lookup__2 = (function (cache__$1,e){if((function (){var and__7556__auto__ = cache__$1;if(and__7556__auto__)
{return cache__$1.cache$core$CacheProtocol$lookup$arity$2;
} else
{return and__7556__auto__;
}
})())
{return cache__$1.cache$core$CacheProtocol$lookup$arity$2(cache__$1,e);
} else
{var x__8195__auto__ = (((cache__$1 == null))?null:cache__$1);return (function (){var or__7568__auto__ = (cache.core.lookup[goog.typeOf(x__8195__auto__)]);if(or__7568__auto__)
{return or__7568__auto__;
} else
{var or__7568__auto____$1 = (cache.core.lookup["_"]);if(or__7568__auto____$1)
{return or__7568__auto____$1;
} else
{throw cljs.core.missing_protocol.call(null,"CacheProtocol.lookup",cache__$1);
}
}
})().call(null,cache__$1,e);
}
});
var lookup__3 = (function (cache__$1,e,not_found){if((function (){var and__7556__auto__ = cache__$1;if(and__7556__auto__)
{return cache__$1.cache$core$CacheProtocol$lookup$arity$3;
} else
{return and__7556__auto__;
}
})())
{return cache__$1.cache$core$CacheProtocol$lookup$arity$3(cache__$1,e,not_found);
} else
{var x__8195__auto__ = (((cache__$1 == null))?null:cache__$1);return (function (){var or__7568__auto__ = (cache.core.lookup[goog.typeOf(x__8195__auto__)]);if(or__7568__auto__)
{return or__7568__auto__;
} else
{var or__7568__auto____$1 = (cache.core.lookup["_"]);if(or__7568__auto____$1)
{return or__7568__auto____$1;
} else
{throw cljs.core.missing_protocol.call(null,"CacheProtocol.lookup",cache__$1);
}
}
})().call(null,cache__$1,e,not_found);
}
});
lookup = function(cache__$1,e,not_found){
switch(arguments.length){
case 2:
return lookup__2.call(this,cache__$1,e);
case 3:
return lookup__3.call(this,cache__$1,e,not_found);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
lookup.cljs$core$IFn$_invoke$arity$2 = lookup__2;
lookup.cljs$core$IFn$_invoke$arity$3 = lookup__3;
return lookup;
})()
;
cache.core.has_QMARK_ = (function has_QMARK_(cache__$1,e){if((function (){var and__7556__auto__ = cache__$1;if(and__7556__auto__)
{return cache__$1.cache$core$CacheProtocol$has_QMARK_$arity$2;
} else
{return and__7556__auto__;
}
})())
{return cache__$1.cache$core$CacheProtocol$has_QMARK_$arity$2(cache__$1,e);
} else
{var x__8195__auto__ = (((cache__$1 == null))?null:cache__$1);return (function (){var or__7568__auto__ = (cache.core.has_QMARK_[goog.typeOf(x__8195__auto__)]);if(or__7568__auto__)
{return or__7568__auto__;
} else
{var or__7568__auto____$1 = (cache.core.has_QMARK_["_"]);if(or__7568__auto____$1)
{return or__7568__auto____$1;
} else
{throw cljs.core.missing_protocol.call(null,"CacheProtocol.has?",cache__$1);
}
}
})().call(null,cache__$1,e);
}
});
cache.core.hit = (function hit(cache__$1,e){if((function (){var and__7556__auto__ = cache__$1;if(and__7556__auto__)
{return cache__$1.cache$core$CacheProtocol$hit$arity$2;
} else
{return and__7556__auto__;
}
})())
{return cache__$1.cache$core$CacheProtocol$hit$arity$2(cache__$1,e);
} else
{var x__8195__auto__ = (((cache__$1 == null))?null:cache__$1);return (function (){var or__7568__auto__ = (cache.core.hit[goog.typeOf(x__8195__auto__)]);if(or__7568__auto__)
{return or__7568__auto__;
} else
{var or__7568__auto____$1 = (cache.core.hit["_"]);if(or__7568__auto____$1)
{return or__7568__auto____$1;
} else
{throw cljs.core.missing_protocol.call(null,"CacheProtocol.hit",cache__$1);
}
}
})().call(null,cache__$1,e);
}
});
cache.core.miss = (function miss(cache__$1,e,ret){if((function (){var and__7556__auto__ = cache__$1;if(and__7556__auto__)
{return cache__$1.cache$core$CacheProtocol$miss$arity$3;
} else
{return and__7556__auto__;
}
})())
{return cache__$1.cache$core$CacheProtocol$miss$arity$3(cache__$1,e,ret);
} else
{var x__8195__auto__ = (((cache__$1 == null))?null:cache__$1);return (function (){var or__7568__auto__ = (cache.core.miss[goog.typeOf(x__8195__auto__)]);if(or__7568__auto__)
{return or__7568__auto__;
} else
{var or__7568__auto____$1 = (cache.core.miss["_"]);if(or__7568__auto____$1)
{return or__7568__auto____$1;
} else
{throw cljs.core.missing_protocol.call(null,"CacheProtocol.miss",cache__$1);
}
}
})().call(null,cache__$1,e,ret);
}
});
cache.core.evict = (function evict(cache__$1,e){if((function (){var and__7556__auto__ = cache__$1;if(and__7556__auto__)
{return cache__$1.cache$core$CacheProtocol$evict$arity$2;
} else
{return and__7556__auto__;
}
})())
{return cache__$1.cache$core$CacheProtocol$evict$arity$2(cache__$1,e);
} else
{var x__8195__auto__ = (((cache__$1 == null))?null:cache__$1);return (function (){var or__7568__auto__ = (cache.core.evict[goog.typeOf(x__8195__auto__)]);if(or__7568__auto__)
{return or__7568__auto__;
} else
{var or__7568__auto____$1 = (cache.core.evict["_"]);if(or__7568__auto____$1)
{return or__7568__auto____$1;
} else
{throw cljs.core.missing_protocol.call(null,"CacheProtocol.evict",cache__$1);
}
}
})().call(null,cache__$1,e);
}
});
cache.core.seed = (function seed(cache__$1,base){if((function (){var and__7556__auto__ = cache__$1;if(and__7556__auto__)
{return cache__$1.cache$core$CacheProtocol$seed$arity$2;
} else
{return and__7556__auto__;
}
})())
{return cache__$1.cache$core$CacheProtocol$seed$arity$2(cache__$1,base);
} else
{var x__8195__auto__ = (((cache__$1 == null))?null:cache__$1);return (function (){var or__7568__auto__ = (cache.core.seed[goog.typeOf(x__8195__auto__)]);if(or__7568__auto__)
{return or__7568__auto__;
} else
{var or__7568__auto____$1 = (cache.core.seed["_"]);if(or__7568__auto____$1)
{return or__7568__auto____$1;
} else
{throw cljs.core.missing_protocol.call(null,"CacheProtocol.seed",cache__$1);
}
}
})().call(null,cache__$1,base);
}
});
cache.core.t = (function t(x){return (x + (1));
});
