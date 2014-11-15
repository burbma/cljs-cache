(ns user
  (:require [weasel.repl.websocket :as websocket]
            [cemerick.piggieback :as piggieback]))

(defn init []
  (piggieback/cljs-repl
   :repl-env (websocket/repl-env
              :ip "localhost" :port 9002)))
