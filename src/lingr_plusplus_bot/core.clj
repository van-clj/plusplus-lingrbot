(ns lingr-plusplus-bot.core
  (:use
    [clojure.data.json :only (read-json)]
    [compojure.core]
    [ring.adapter.jetty]))

(defroutes
  hello
  (GET "/" [] "plus plus")
  (POST "/"
        {body :body}
        (let [message (:message (first (:events (read-json (slurp body)))))]
          (str (:text message) "++"))))

(defn -main []
  (run-jetty hello {:port 4003}))
