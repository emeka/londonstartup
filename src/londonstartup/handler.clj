(ns londonstartup.handler
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [londonstartup.models :as models]
            [londonstartup.controllers.startups :as startup]
            [londonstartup.controllers.users :as user]
            [londonstartup.controllers.home :as home]
            [londonstartup.controllers.auth :as auth]
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
  (GET "/" [] (home/home))

  ;; Startups Actions
  (GET "/startups" [query] (startup/startups query))
  (GET "/startups/:website" [website] (startup/startup website))
  (POST "/startups" [:as {startup :params}] (startup/startup-new startup))
  (PUT "/startups/:_website" [_website :as {startup :params}] (startup/startup-update startup _website))
  (DELETE "/startups/:website" [website] (startup/startup-delete website))

  ;; Startup Forms
  (GET "/add/startups" [] (nr/restricted (startup/add-startup-form)))
  (GET "/update/startups/:website" [website] (nr/restricted (startup/update-startup-form website)))

  ;; Users Actions
  (GET "/users" [query] (user/users query))
  (GET "/users/:username" [username] (user/user username))
  ;(GET "/users/:username/profile" [username] (user/user-profile username))
  (POST "/users" [uri :as {user :params}] (user/user-new user uri))
  (PUT "/users/:_username" [_username :as {user :params}] (user/user-update user _username))
  (DELETE "/users/:username" [username] (user/user-delete username))

  ;; Users Forms
  (GET "/signup" [uri] (nr/restricted (user/signup uri)))
  (GET "/update/users/:username" [username] (nr/restricted (user/update-user-form username)))

  ;;Log In
  (GET "/login" [uri oauth_token oauth_verifier denied auto] (auth/login uri oauth_token oauth_verifier denied auto))

  ;;Log Out
  (GET "/logout" [] (auth/logout))

  ;;Static Resources
  (route/resources "/"))

(defn redirect-to-login [{:keys [uri]}]
  (resp/redirect (str "/login?auto=true&uri=" uri)))

(defn user-logged? [req]
  (session/user-logged?))

(def app
  ;;The first parameter of app-handler must be a sequence
  (nm/app-handler [app-routes] :access-rules [{:on-fail redirect-to-login :rules [user-logged?]}]))

(defn init []
  (models/initialize))