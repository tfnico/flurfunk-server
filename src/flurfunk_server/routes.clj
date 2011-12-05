(ns flurfunk-server.routes
  "The routes of Flurfunk."
  (:use compojure.core
        ring.util.servlet)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [flurfunk-server.marshalling :as ms]
            [flurfunk-server.storage :as storage]))

(defn- parse-message [s]
    (ms/unmarshal-message (ms/parse-xml s)))

(defroutes main-routes
  (GET "/" {uri :uri}
       {:status 302 :headers {"Location" (str uri
                                              (if (not (.endsWith uri "/")) "/")
                                              "index.html")}})
  (GET "/messages" {params :params}
       (ms/marshal-messages
        (if-let [since (:since params)]
          (do
            (storage/get-messages {:since (Long. since)}))
          (storage/get-messages))))
  (GET "/message/:id" [id]
       (if-let [message (storage/find-message id)]
         (ms/marshal-message message)
         {:body "" :status 404}))
  (POST "/message" {body :body}
        (storage/add-message (conj (parse-message body)
                                   {:timestamp (System/currentTimeMillis)}))
        "")
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
     (handler/site main-routes))