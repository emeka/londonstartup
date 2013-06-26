(ns londonstartup.handler
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [londonstartup.models :as models]
            [londonstartup.controllers.startups :as controllers]
            [ring.util.response :as resp]
            [noir.util.middleware :as nm]
            )
  (:use compojure.core)
  (import org.bson.types.ObjectId))

;;; Routing
(defroutes app-routes
  (GET "/" [] (resp/redirect "/startups"))
  (GET "/startups" [] (controllers/startups))
  (GET "/startups/:website" [website] (controllers/startup website))
  (POST "/startups" [:as {startup :params}] (controllers/startup-new startup))
  (PUT "/startups/:_website" [:as {startup :params}] (controllers/startup-update startup))
  (DELETE "/startups/:website" [website] (controllers/startup-delete website))
;;Forms
  (GET "/add/startups" [] (controllers/add-startup-form))
  (GET "/update/startups/:website" [website] (controllers/update-startup-form website))
;;Static Resources
  (route/resources "/"))

(def app
  (nm/app-handler [app-routes])) ;; the app-handler breaks the API as it require a vector of routes.

(defn init []
  (models/initialize))