(ns lingr-plusplus-bot.core
  (:gen-class)
  (:use
    [clojure.data.json :only (read-json json-str)]
    [compojure.core]
    [clojure.contrib.duck-streams :only (reader read-lines writer)]
    [ring.adapter.jetty]))

(def plusplus (atom (hash-map)))

(defn event2response [event]
  (let [message (:message event)
        plus (second (re-find #"([a-zA-Z0-9_-]+)\+\+$" (:text message)))
        minus (second (re-find #"([a-zA-Z0-9_-]+)--$" (:text message)))
        pluseq (nth (re-find #"([a-zA-Z0-9_-]+)\+=([0-9])$" (:text message)) 3)
        minuseq (nth (re-find #"([a-zA-Z0-9_-]+)-=([0-9])$" (:text message)) 3)]
    (cond
      plus (let [cnt (+ (or (get @plusplus plus) 0) 1)]
             (swap! plusplus assoc plus cnt)
             (str plus "++ (" cnt ")"))
      minus (let [cnt (+ (or (get @plusplus minus) 0) -1)]
              (swap! plusplus assoc minus cnt)
              (str minus "-- (" cnt ")"))
      pluseq (let [cnt (+ (get @plusplus pluseq) 0)]
               (swap! plusplus assoc pluseq cnt)
               (str pluseq "+=" pluseq "(" cnt ")"))
      minuseq (let [cnt (- (get @plusplus minuseq) 0)]
                (swap! plusplus assoc minuseq cnt)
                (str minuseq "-=" minuseq "(" cnt ")"))
      :else ""))))

(defroutes
  hello
  (GET "/" [] "plus plus")
  (POST "/"
        {body :body}
        (apply str (interpose "\n" (map event2response (:events (read-json (slurp body))))))))

(defn -main []
  (with-open [r (reader "plusplus.json")]
    (reduce (fn [m [k v]]
      (swap! plusplus assoc k v)), {}, (read-json r false)))
  (defonce server (run-jetty hello {:port 4003 :join? false}))
  (.addShutdownHook (Runtime/getRuntime) (Thread. (fn []
    (spit "plusplus.json" (json-str @plusplus)) (.stop server))))
  (.start server))
