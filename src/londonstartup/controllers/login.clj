(ns londonstartup.controllers.login
  (:require [ring.util.response :as resp]
            [londonstartup.common.result :as result]
            [londonstartup.services.twitter :as twitter]
            [londonstartup.models.users :as users ]
            [noir.session :as session]))



(defn login [{:keys [oauth_token oauth_verifier] :as auth}]
  (if (or (not oauth_token) (not oauth_verifier))
    (let [request-token (twitter/request-token "http://staging.startupdirectory.org/login")]
      (if (not (result/has-error? request-token))
        (let [request-token (result/value request-token)]
          (session/put! :request-token request-token)
          (resp/redirect (result/value (twitter/approval-url request-token))))
        (str request-token)))
    (let [oauth_token-secret (result/value (twitter/access-token-secret (session/get! :request-token oauth_verifier)))]
      (session/put! :user {:auth {:access-token oauth_token :access-token-secret oauth_token-secret}})
      (session/get :user))))
