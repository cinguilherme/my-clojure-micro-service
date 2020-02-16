(ns this-one.dbhelpers
  (:require  [monger.core :as mg]
             [monger.collection :as mc]
             [monger.json]))

(def mongo-uri "mongodb://aashrey:admin123@127.0.0.1:27017/sample")

(defn db-get-project [proj-name]
  (let [connect-string mongo-uri
        {:keys [conn db]} (mg/connect-via-uri connect-string)]
    (mc/find-maps db "catalog" {:proj-name proj-name})))

(defn get-all-projects []
  (let [uri mongo-uri {:keys [conn db]}
        (mg/connect-via-uri uri)]
    (mc/find-maps db "catalog")))