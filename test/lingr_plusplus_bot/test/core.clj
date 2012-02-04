(ns lingr-plusplus-bot.test.core
  (:use [lingr-plusplus-bot.core])
  (:use [clojure.string :only (trim)])
  (:use [clojure.test]))

(deftest test-plusplus
  (swap! plusplus assoc "thinca" 0)
  (swap! plusplus assoc "mattn" 0)
  (is (= (trim (event2response {:message {:text "mattn++"}})) "mattn++ (1)"))
  (is (= (trim (event2response {:message {:text "thinca++"}})) "thinca++ (1)"))
  (is (= (trim (event2response {:message {:text "mattn++"}})) "mattn++ (2)"))
)

(deftest test-minusminus
  (swap! plusplus assoc "thinca" 3)
  (swap! plusplus assoc "mattn" 2)
  (is (= (trim (event2response {:message {:text "mattn--"}})) "mattn-- (1)"))
  (is (= (trim (event2response {:message {:text "thinca--"}})) "thinca-- (2)"))
  (is (= (trim (event2response {:message {:text "mattn--"}})) "mattn-- (0)"))
)

(deftest test-plusequal
  (swap! plusplus assoc "ujihisa" 20000)
  (swap! plusplus assoc "mattn" 0)
  (is (= (trim (event2response {:message {:text "mattn+=3"}})) "mattn+=3 (3)"))
  (is (= (trim (event2response {:message {:text "ujihisa+=5"}})) "ujihisa+=5 (20005)"))
)

(deftest test-minusequal
  (swap! plusplus assoc "ujihisa" 20000)
  (swap! plusplus assoc "mattn" 0)
  (is (= (trim (event2response {:message {:text "mattn-=3"}})) "mattn-=3 (-3)"))
  (is (= (trim (event2response {:message {:text "ujihisa-=20000"}})) ""))
  (is (= (trim (event2response {:message {:text "ujihisa-=2"}})) "ujihisa-=2 (19998)"))
)
