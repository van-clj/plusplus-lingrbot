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
        pluseq (re-find #"([a-zA-Z0-9_-]+)\+=([0-9])$" (:text message))
        minuseq (re-find #"([a-zA-Z0-9_-]+)\-=([0-9])$" (:text message))]
    (cond
      plus (let [cnt (+ (or (get @plusplus plus) 0) 1)]
             (swap! plusplus assoc plus cnt)
             (str plus "++ (" cnt ")"))
      minus (let [cnt (+ (or (get @plusplus minus) 0) -1)]
              (swap! plusplus assoc minus cnt)
              (str minus "-- (" cnt ")"))
      pluseq (let [cnt (+ (or (get @plusplus (nth pluseq 1)) 0)
                          (Integer/parseInt (nth pluseq 2)))]
                 (swap! plusplus assoc (nth pluseq 1) cnt)
                 (str (nth pluseq 1) "+=" (nth pluseq 2) " (" cnt ")"))
      minuseq (let [cnt (- (or (get @plusplus (nth minuseq 1)) 0)
                           (Integer/parseInt (nth minuseq 2)))]
                 (swap! plusplus assoc (nth minuseq 1) cnt)
                 (str (nth minuseq 1) "-=" (nth minuseq 2) " (" cnt ")"))
      :else "")))

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
