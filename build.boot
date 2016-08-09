(set-env!
 :source-paths #{"src"}
 :resource-paths #{"resources"}

 :dependencies '[[org.clojure/clojure "1.9.0-alpha10"]
                 [org.clojure/core.async "0.2.385"]
                 [org.clojure/clojurescript "1.9.89"]
                 [org.clojure/tools.namespace "0.2.11" :scope "test"]
                 [org.clojure/tools.nrepl "0.2.12" :scope "test"]    ;; needed by bREPL

                 [pandeiro/boot-http "0.7.3" :scope "test"]
                 [adzerk/boot-cljs "1.7.228-1" :scope "test"]
                 [adzerk/boot-cljs-repl "0.3.2" :scope "test"]
                 [adzerk/boot-reload "0.4.11" :scope "test"]
                 [com.cemerick/piggieback "0.2.1" :scope "test"]     ;; needed by bREPL
                 [tailrecursion/cljs-priority-map "1.2.0"]
                 [weasel "0.7.0" :scope "test"]                      ;; needed by bREPL
                 ])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl cljs-repl-env start-repl]]
         '[adzerk.boot-reload :refer [reload]]
         '[clojure.tools.namespace.repl :refer [set-refresh-dirs]]
         '[pandeiro.boot-http :refer [serve]])

(deftask dev
  []
  (set-env! :source-paths #(conj % "test"))
  (apply set-refresh-dirs (get-env :directories))
  (comp
   (serve :dir "target/cache/")
   (watch)
   (cljs-repl)
   (reload)
   (cljs)
   (target :dir #{"target"})))
