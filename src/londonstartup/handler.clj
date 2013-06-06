(ns londonstartup.handler
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [londonstartup.models :as models]
            [londonstartup.controllers.startups :as controllers]
            [noir.util.middleware :as nm]
            )
  (:use compojure.core))

;;; Routing
(defroutes app-routes
  (GET "/startups" [] (controllers/startups))
  (GET "/startup/:website" [website] (controllers/startup website))
  (POST "/startups" [website :as {new-startup :params}] (controllers/startup-new website new-startup))
  (PUT "/startup/:_website" [website _id :as {updated-startup :params}] (controllers/startup-update website _id updated-startup))
  (DELETE "/startup/:website" [website] (controllers/startup-delete website)))

(def app
  (nm/app-handler [app-routes])) ;; the app-handler breaks the API as it require a vector of routes.

(defn init []
  (models/initialize))