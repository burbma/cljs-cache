(set-env!
 :source-paths #{"src"}
 :resource-paths #{"resources"}

 :dependencies '[[org.clojure/clojure "1.9.0-alpha10"]
                 [org.clojure/clojurescript "1.9.89"]
                 [org.clojure/tools.namespace "0.2.11" :scope "test"]
                 [org.clojure/tools.nrepl "0.2.12" :scope "test"]    ;; needed by bREPL

                 [adzerk/boot-cljs "1.7.228-1" :scope "test"]
                 [adzerk/boot-cljs-repl "0.3.2" :scope "test"]
                 [adzerk/bootlaces "0.1.13" :scope "test"]
                 [adzerk/boot-reload "0.4.11" :scope "test"]
                 [adzerk/boot-test "1.1.2"]
                 [com.cemerick/piggieback "0.2.1" :scope "test"]     ;; needed by bREPL
                 [crisptrutski/boot-cljs-test "0.2.2-SNAPSHOT"]
                 [pandeiro/boot-http "0.7.3" :scope "test"]
                 [tailrecursion/cljs-priority-map "1.2.1"]
                 [weasel "0.7.0" :scope "test"]                      ;; needed by bREPL
                 ])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl cljs-repl-env start-repl]]
         '[adzerk.bootlaces :refer :all]
         '[adzerk.boot-reload :refer [reload]]
         '[crisptrutski.boot-cljs-test :refer [test-cljs exit!]]
         '[clojure.tools.namespace.repl :refer [set-refresh-dirs]]
         '[pandeiro.boot-http :refer [serve]])


(def +version+ "0.1.4")

(bootlaces! +version+)

(task-options!
 pom {:project     'org.clojars.mmb90/cljs-cache
      :version     +version+
      :description "Port of clorjure/core.cache"
      :url         "https://github.com/burbma/cljs-cache"
      :scm         {:url "https://github.com/burbma/cljs-cache"}
      :license     {"Eclipse Public License 1.0"
                    "http://opensource.org/licenses/eclipse-1.0.php"}})

(deftask testing
  "Conj test path to environment."
  []
  (merge-env! :source-paths #{"test"})
  identity)

(deftask test-all
  "Run tests on phantomjs."
  []
  (comp (testing)
        (test-cljs)
        (exit!)))

(deftask dev
  "Launch immediate feedback dev environment."
  []
  (apply set-refresh-dirs (get-env :directories))
  (comp
   (testing)
   (serve :dir "target/cache/")
   (watch)
   (cljs-repl)
   (reload)
   (cljs)
   (target :dir #{"target"})))

(deftask dev-testing
  "Launch immediate feedback dev environment that also runs tests each
  time. Alternatively (preferably in my case) you can run `boot dev` and
  include `(run-tests)` at the bottom of core_test.cljs and the tests will run
  in the browser and you can see the results in the console."
  []
  (apply set-refresh-dirs (get-env :directories))
  (comp
   (testing)
   (serve :dir "target/cache/")
   (watch)
   (cljs-repl)
   (reload)
   (test-cljs)
   (cljs)
   (target :dir #{"target"})))
