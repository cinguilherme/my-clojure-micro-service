(ns this-one.dbhelpers
  (:require  [monger.core :as mg]
             [monger.collection :as mc]
             [monger.json]))

(def mongo-uri "mongodb://aashrey:admin123@127.0.0.1:27017/sample")

(defn db-get-project [proj-name]
    (mc/find-maps (db) "catalog" {:proj-name proj-name}))

(defn get-all-projects []
    (mc/find-maps (db) "catalog"))

(defn create-project [] ())

(defn db []
  (let [connect-string mongo-uri {:keys [conn db]} (mg/connect-via-uri connect-string)] db))


;; String s = new String()
(.toUpperCase (String. "lili"))
(.substring (String. "lili lili") 2 5)
(class (String.))
(System/getenv "MONGO")
(.toString (doto (StringBuffer.) (.append "alex") (.append "jr") (.append "santos")))