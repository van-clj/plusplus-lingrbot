(ns lingr-plusplus-bot.core
  (:use
    [clojure.data.json :only (read-json)]
    [compojure.core]
    [ring.adapter.jetty]))

(def cnt (atom 0))
(defroutes
  hello
  (GET "/" [] "plus plus")
  (POST "/"
        {body :body}
        (let [message (:message (first (:events (read-json (slurp body)))))]
          (swap! cnt inc)
          (let [target (second (re-find #"(\w+)\+\+$" (:text message)))]
            (if target
              (str target "++ (" @cnt ")")
              "")))))

(defn -main []
  (run-jetty hello {:port 4003}))
