(ns londonstartup.controllers.auth
  (:require [ring.util.response :as resp]
            [londonstartup.common.result :as result]
            [londonstartup.services.twitter :as twitter]
            [londonstartup.models.users :as users]
            [londonstartup.views.auth :as views]
            [londonstartup.services.session :as session]))



(defn callback [uri]
  (str (get (System/getenv) "BASE_URL" (get (System/getenv) "URL")) "/login?uri=" uri))

(defn login [uri oauth_token oauth_verifier]
  (let [uri (if uri uri "/")]
    (if (session/user-logged?)
      (resp/redirect uri)
      (if (or (not oauth_token) (not oauth_verifier))
        ;;This is called by us
        (let [request-token (twitter/request-token (callback uri))]
          (if (not (result/has-error? request-token))
            (let [request-token (result/value request-token)]
              (if (= "true" (:oauth_callback_confirmed request-token))
                (do
                  (session/put! :request-token request-token)
                  (resp/redirect (result/value (twitter/approval-url request-token))))
                (str "ERROR")))
            (str request-token)))
        ;;This will be called by Twitter
        (let [request-token (session/get! :request-token )
              twitter_auth (result/value (twitter/access-token-secret request-token oauth_verifier))]
          (if (= oauth_token (:oauth_token request-token))
            (do
              (session/user-logged! {:auth {:twitter twitter_auth}})
              (resp/redirect uri))
            (str "ERROR")))))))

(defn logout []
  (session/user-logout!)
  (resp/redirect "/"))