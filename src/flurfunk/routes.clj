(ns flurfunk.routes
  "The routes of Flurfunk."
  (:use compojure.core
        ring.util.servlet
        hiccup.core
        [hiccup.middleware :only (wrap-base-url)])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [flurfunk.marshalling :as ms]
            [flurfunk.storage :as storage]))

(defn- parse-message [s]
    (ms/unmarshal-message (ms/parse-xml s)))

(defroutes main-routes
  (GET "/" {context :context} (html
               [:head
                [:meta {:charset "utf-8"}]
                [:title "Flurfunk"]
                [:link {:rel "stylesheet" :type "text/css"
                        :href (str context "/flurfunk.css")}]]
               [:body
                [:script {:src (str context "/flurfunk.js")}]]))
  (GET "/messages" []
       (ms/marshal-messages (storage/get-messages)))
  (GET "/message/:id" [id]
       (if-let [message (storage/find-message id)]
         (ms/marshal-message message)
         {:body "" :status 404}))
  (POST "/message" {body :body} (storage/add-message (parse-message body))
        "")
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
     (-> (handler/site main-routes)
         (wrap-base-url)))
