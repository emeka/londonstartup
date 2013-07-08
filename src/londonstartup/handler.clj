(ns londonstartup.handler
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [londonstartup.models :as models]
            [londonstartup.controllers.startups :as startup-controllers]
            [londonstartup.controllers.users :as user-controllers]
            [londonstartup.controllers.home :as home-controllers]
            [londonstartup.controllers.login :as login-controllers]
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
  (PUT "/startups/:_website" [_website :as {startup :params}] (startup-controllers/startup-update startup _website))
  (DELETE "/startups/:website" [website] (startup-controllers/startup-delete website))

;; Startup Forms
  (GET "/add/startups" [] (startup-controllers/add-startup-form))
  (GET "/update/startups/:website" [website] (startup-controllers/update-startup-form website))

;; Users Actions
  (GET "/users" [query] (user-controllers/users query))
  (GET "/users/:username" [username] (user-controllers/user username))
  (POST "/users" [:as {user :params}] (user-controllers/user-new user))
  (PUT "/users/:_username" [_username :as {user :params}] (user-controllers/user-update user _username))
  (DELETE "/users/:username" [username] (user-controllers/user-delete username))

;; Users Forms
  (GET "/add/users" [] (user-controllers/add-user-form))
  (GET "/update/users/:username" [username] (user-controllers/update-user-form username))

;;Log In
  (GET "/login" [& auth] (login-controllers/login [auth]))

;;Static Resources
  (route/resources "/"))

(def app
  (nm/app-handler [app-routes])) ;; the app-handler breaks the API as it require a vector of routes.

(defn init []
  (models/initialize))