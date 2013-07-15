(ns londonstartup.controllers.auth
  (:require [ring.util.response :as resp]
            [londonstartup.common.result :as result]
            [londonstartup.services.twitter :as twitter]
            [londonstartup.models.users :as users]
            [londonstartup.views.auth :as views]
            [londonstartup.services.session :as session]
            [londonstartup.environment :as env]))



(defn- ^{:testable true} callback [redirect_to]
  (let [base-url (str (env/get "BASE_URL" (env/get "URL")))
        redirect_to (if (empty? redirect_to) "/" redirect_to)]
    (if (not (empty? base-url))
      (result/result (str base-url "/login?auto=true&redirect_to=" redirect_to))
      (result/error redirect_to :redirect_to "BASE_URL and URL environment not defined"))))

(defn- ^{:testable true} twitter-auth->user [{:keys [screen_name] :as auth}]
  (result/result {:username screen_name :auth {:twitter auth}}))

(defn- ^{:testable true} find-user [user]
  (let [user-id (get-in user [:auth :twitter :user_id ])]
    (users/user {"auth.twitter.user_id" user-id})))

(defn- ^{:testable true} update-user! [db-user session-user]
  (let [session-twitter-auth (get-in session-user [:auth :twitter ])
        db-twitter-auth (get-in db-user [:auth :twitter ])]
    (if (= (:user_id session-twitter-auth) (:user_id db-twitter-auth))
      (if (not= session-twitter-auth db-twitter-auth)
        (users/update! (merge db-user session-user))
        (result/value db-user))
      (result/error {:db-user db-user :session-user session-user} :user_id "Twitter user_id values do not match"))))

(defn- ^{:testable true} get-request-token [uri]
  (result/until-error->
    (callback uri)
    (twitter/request-token)))

(defn- ^{:testable true} confirmed? [request-token]
  (if (= "true" (:oauth_callback_confirmed request-token))
    (result/result request-token)
    (result/error request-token :oauth_callback_confirmed "Request Token not confirmed")))

(defn- ^{:testable true} session-put! [value key]
  (do
    (session/put! key value)
    (result/result value)))

(defn- ^{:testable true} session-get [key]
  (result/result (session/get key)))

(defn- ^{:testable true} get-approval-url [redirect_to]
  (result/until-error->
    (get-request-token redirect_to)
    (confirmed?)
    (session-put! :request-token )
    (twitter/approval-url)))

(defn- ^{:testable true} redirect-to-twitter-auth-page [redirect_to]
  (let [approval-url-result (get-approval-url redirect_to)]
    (if (result/error-free? approval-url-result)
      (resp/redirect (result/value approval-url-result))
      (str "ERROR: could not get Twitter approval URL" approval-url-result))))

(defn- ^{:testable true} log-existing-user [db-user session-user redirect_to]
  (let [update-result (update-user! db-user session-user)]
    (if (result/error-free? update-result)
      (do
        (session/put! :user (result/value update-result))
        (resp/redirect redirect_to))
      (str "ERROR: Cannot update user: " update-result))))


(defn- ^{:testable true} log-new-user [session-user redirect_to]
  (let [add-result (users/add! session-user)]
    (if (result/error-free? add-result)
      (do
        (session/put! :user (result/value add-result))
        (resp/redirect (str "/signup?redirect_to=" redirect_to)))
      (str "ERROR: cannot add user: " add-result))))

(defn- ^{:testable true} validate-oauth-token [request-token oauth-token]
  (if (= oauth-token (:oauth_token request-token))
    (result/result request-token)
    (result/error {:oauth-token oauth-token :request-token request-token} :oauth_token "oauth_token values do not match")))

(defn- ^{:testable true} create-session-user [oauth_token oauth_verifier]
  (result/until-error->
    (session-get :request-token )
    (validate-oauth-token oauth_token)
    (twitter/access-token-secret oauth_verifier)
    (twitter-auth->user)))

(defn- ^{:testable true} authorise-user [oauth_token oauth_verifier redirect_to]
  (let [session-user-result (create-session-user oauth_token oauth_verifier)]
    (if (result/error-free? session-user-result)
      (let [session-user (result/value session-user-result)
            db-user-result (find-user (result/value session-user-result))]
        (if (result/error-free? db-user-result)
          (log-existing-user (result/value db-user-result) session-user redirect_to)
          (log-new-user session-user redirect_to)))
      (str "ERROR: Cannot create session user: " session-user-result))))

(defn login [redirect_to oauth_token oauth_verifier denied auto]
  (let [redirect_to (if (empty? redirect_to) "/" redirect_to)]
    (cond
      (session/get :user ) (resp/redirect redirect_to)
      (not (empty? denied)) (do
                            (session/clear!)
                            (session/flash! "ACCESS DENIED")
                            (views/login-page))
      (or (empty? auto) (= "false" (clojure.string/lower-case auto))) (views/login-page)
      (or (not oauth_token) (not oauth_verifier)) (redirect-to-twitter-auth-page redirect_to)
      :else (authorise-user oauth_token oauth_verifier redirect_to))))

(defn logout []
  (session/clear!)
  (resp/redirect "/"))