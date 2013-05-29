(ns lingr-plusplus-bot.core
  (:gen-class)
  (:use
    [clojure.data.json :only (read-json json-str)]
    [compojure.core]
    [clojure.contrib.duck-streams :only (reader read-lines writer)]
    [clojure.contrib.string :only (lower-case)]
    [ring.adapter.jetty]))

(def plusplus (atom (hash-map)))
(defn event2response [event]
  (let [message (:message event)
        plus (second (re-find #"([^+ ]+)\+\+$" (:text message)))
        minus (second (re-find #"([^+ ]+)--$" (:text message)))
        pluseq (re-find #"([^+ ]+)\+=([0-9]+)$" (:text message))
        minuseq (re-find #"([^+ ]+)\-=([0-9]+)$" (:text message))
        graph (re-find #"^\+\+\?$" (:text message))]
   (cond
      plus (let [user (lower-case plus)
                 cnt (+ (or (get @plusplus user) 0) 1)]
             (swap! plusplus assoc user cnt)
             (str user "++ (" cnt ")"))
      minus (let [user (lower-case minus)
                  cnt (+ (or (get @plusplus user) 0) -1)]
              (swap! plusplus assoc user cnt)
              (str user "-- (" cnt ")"))
      pluseq (let [user (lower-case (nth pluseq 1))
                   value (nth pluseq 2)
                   cnt (+ (or (get @plusplus user) 0)
                          (Integer/parseInt value))]
                 (swap! plusplus assoc user cnt)
                 (str user "+=" value " (" cnt ")"))
      minuseq (let [user (lower-case (nth minuseq 1))
                    value (nth minuseq 2)
                    cnt (- (or (get @plusplus user) 0)
                           (Integer/parseInt value))]
                 (swap! plusplus assoc user cnt)
                 (str user "-=" value " (" cnt ")"))
      graph (let [k (keys @plusplus)]
          (str
            "https://chart.googleapis.com/chart?cht=p3&chd=t:"
            (clojure.string/join "," (for [kk k] (@plusplus kk)))
            "&chs=250x100&chl="
            (clojure.string/join "%7c" k)
            "#.jpg"))
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
      (swap! plusplus assoc (lower-case k) v)), {}, (read-json r false)))
  (defonce server (run-jetty hello {:port 4003 :join? false}))
  (.addShutdownHook (Runtime/getRuntime) (Thread. (fn []
    (spit "plusplus.json" (json-str @plusplus)) (.stop server))))
  (.start server))
