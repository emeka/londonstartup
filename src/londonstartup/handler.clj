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


(defn redirect-url
  ([redirect_to] (redirect-url redirect_to "/"))
  ([redirect_to default] (if (not (empty? redirect_to)) redirect_to default)))

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

  ;; User Admin
  (GET "/users" [query] (nr/restricted (user/users query)))
  (GET "/users/:username" [username] (nr/restricted (user/user username))) ; == profile
  (GET "/users/:username/settings" [username redirect_to :as {uri :uri}]
    (nr/restricted (user/user-settings uri (redirect-url redirect_to uri) username)))
  (PUT "/users/:_username/settings" [_username redirect_to :as {uri :uri settings :params}]
    (nr/restricted (user/user-settings-update settings uri (redirect-url redirect_to uri) _username)))
  (DELETE "/users/:username" [username :as {uri :uri}] (nr/restricted (user/user-delete username)))

  ;; User Settings
  (GET "/settings" [redirect_to :as {uri :uri}] (nr/restricted (user/user-settings uri (redirect-url redirect_to uri))))
  (PUT "/settings" [redirect_to :as {uri :uri settings :params}] (user/user-settings-update settings uri (redirect-url redirect_to uri)))

  ;; Users Forms
  (GET "/signup" [redirect_to] (nr/restricted (user/signup (redirect-url redirect_to))))

  ;;Log In
  (GET "/login" [redirect_to oauth_token oauth_verifier denied auto]
    (auth/login (redirect-url redirect_to) oauth_token oauth_verifier denied auto))

  ;;Log Out
  (GET "/logout" [] (auth/logout))

  ;;Static Resources
  (route/resources "/"))

(defn redirect-to-login [{:keys [uri]}]
  (resp/redirect (str "/login?auto=true&redirect_to=" uri)))

(defn user-logged? [req]
  (session/get :user ))

(def app
  ;;The first parameter of app-handler must be a sequence
  (nm/app-handler [app-routes] :access-rules [{:on-fail redirect-to-login :rules [user-logged?]}]))

(defn init []
  (models/initialize))