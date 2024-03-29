(ns greenroids.core
  (:use [compojure.core])
  (:use [hiccup.page :only [html5 include-css include-js]])
  (:use [hiccup.element :only [javascript-tag]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler])
  (:use [ring.adapter.jetty :only [run-jetty]]))

(defn game
  "Serves the main game page."
  []
  (html5 [:head
          [:meta {:charset "utf-8"}]
          [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
          [:title "GreenRoids"]
          (include-js "js/gee.min.js")
          (include-js "js/cljs.js")]
         [:body {:onload "greenroids.core.start();"}]))

(defroutes main-routes
  (GET "/" [] (game))
  (route/resources "/")
  (route/not-found "Page not found!"))

(def app (handler/site main-routes))

(defn -main [port]
  (run-jetty app {:port (Integer. port)}))

;;(defonce server (run-jetty #'app {:port 8080 :join? false}))
