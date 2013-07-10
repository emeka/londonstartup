(ns londonstartup.handler
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [londonstartup.models :as models]
            [londonstartup.controllers.startups :as startup-controllers]
            [londonstartup.controllers.users :as user-controllers]
            [londonstartup.controllers.home :as home-controllers]
            [londonstartup.controllers.auth :as auth-controllers]
            [londonstartup.services.session :as session]
            [ring.util.response :as resp]
            [noir.util.middleware :as nm]
            [noir.util.route :as nr]
            )
  (:use compojure.core)
  (import org.bson.types.ObjectId))


; /startups
; /funds

; /starters
; /angels
; /funds

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
  (GET "/add/startups" [] (nr/restricted (startup-controllers/add-startup-form)))
  (GET "/update/startups/:website" [website] (nr/restricted (startup-controllers/update-startup-form website)))

  ;; Users Actions
  (GET "/users" [query] (user-controllers/users query))
  (GET "/users/:username" [username] (user-controllers/user username))
  (POST "/users" [:as {user :params}] (user-controllers/user-new user))
  (PUT "/users/:_username" [_username :as {user :params}] (user-controllers/user-update user _username))
  (DELETE "/users/:username" [username] (user-controllers/user-delete username))

  ;; Users Forms
  (GET "/add/users" [] (nr/restricted (user-controllers/add-user-form)))
  (GET "/update/users/:username" [username] (nr/restricted (user-controllers/update-user-form username)))

  ;;Log In
  (GET "/login" [& params] (auth-controllers/login params))

  ;;Log Out
  (GET "/logout" [] (auth-controllers/logout))

  ;;Static Resources
  (route/resources "/"))

(defn redirect-to-login [{:keys [uri]}]
  (resp/redirect (str "/login?uri=" uri)))

(defn user-logged? [req]
  (session/user-logged?))

(def app
  ;;The first parameter of app-handler must be a sequence
  (nm/app-handler [app-routes] :access-rules [{:on-fail redirect-to-login :rules [user-logged?]}]))

(defn init []
  (models/initialize))