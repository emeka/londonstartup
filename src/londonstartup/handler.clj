(ns londonstartup.handler
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [londonstartup.models :as models]
            [londonstartup.controllers.startups :as startup-controllers]
            [londonstartup.controllers.home :as home-controllers]
            [ring.util.response :as resp]
            [noir.util.middleware :as nm]
            )
  (:use compojure.core)
  (import org.bson.types.ObjectId))

;;; Routing
(defroutes app-routes
;;Home Page
  (GET "/" [] (home-controllers/home))

;; Startups Actions
  (GET "/startups" [query] (startup-controllers/startups query))
  (GET "/startups/:website" [website] (startup-controllers/startup website))
  (POST "/startups" [:as {startup :params}] (startup-controllers/startup-new startup))
  (PUT "/startups/:_website" [:as {startup :params}] (startup-controllers/startup-update startup))
  (DELETE "/startups/:website" [website] (startup-controllers/startup-delete website))

;; Startup Forms
  (GET "/add/startups" [] (startup-controllers/add-startup-form))
  (GET "/update/startups/:website" [website] (startup-controllers/update-startup-form website))

;;Static Resources
  (route/resources "/"))

(def app
  (nm/app-handler [app-routes])) ;; the app-handler breaks the API as it require a vector of routes.

(defn init []
  (models/initialize))