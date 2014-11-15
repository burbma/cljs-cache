(defproject com.galebach/cljs-cache "0.1.0-SNAPSHOT"
  :description "Clojurescript port of clojure.core/cache"
  :url "http://galebach.com/cljs-cache"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2311"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [weasel "0.4.2"]]

  :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]
            [com.cemerick/clojurescript.test "0.3.1"]]

  :source-paths ["src"]

  :profiles {:dev {:source-paths ["dev"]
                   :plugins [[com.cemerick/austin "0.1.5"]]}}

  :cljsbuild {
    :builds [{:id "cache"
              :source-paths ["src"]
              :compiler {
                :output-to "cache.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
