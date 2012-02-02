(ns lingr-plusplus-bot.core
  (:use
    [clojure.data.json :only (read-json)]
    [compojure.core]
    [ring.adapter.jetty]))

(def plusplus (atom {}))
(defroutes
  hello
  (GET "/" [] "plus plus")
  (POST "/"
        {body :body}
        (let [message (:message (first (:events (read-json (slurp body)))))
              target (second (re-find #"(\w+)\+\+$" (:text message)))]
          (if target
            (let [cnt (+ 1 (or (get @plusplus target) 0))]
              (swap! plusplus assoc target cnt)
              (str target "++ (" cnt ")"))
            ""))))

(defn -main []
  (run-jetty hello {:port 4003}))
