(ns londonstartup.services.twitter
  (:require [oauth.client :as oauth]
            [londonstartup.common.result :as result]
            [clojure.data.json :as json])
  (:use [twitter.oauth]
        [twitter.callbacks]
        [twitter.callbacks.handlers]
        [twitter.api.restful])
  (:import (twitter.callbacks.protocols SyncSingleCallback)))


(def app-consumer-key "V4pHVpupNNtj0GYwcglzQ")
(def app-consumer-secret (get (System/getenv) "TWITTER_APP_CONSUMER_SECRET"))
(def user-access-token "19400021-Aqbp1v1MmxMGiPEI8qUhOEfVHAGsoIXNXs8yGCvF4")
(def user-access-token-secret (get (System/getenv) "TWITTER_ACCESS_TOKEN_SECRET"))


;;Login
(def consumer (oauth/make-consumer
                app-consumer-key
                app-consumer-secret
                "http://api.twitter.com/oauth/request_token"
                "http://api.twitter.com/oauth/access_token"
                "http://api.twitter.com/oauth/authorize"
                :hmac-sha1 ))

(defn request-token [callback]
  (try
    (result/result (oauth/request-token consumer callback))
    (catch Exception e (result/error {:callback callback :consumer consumer} :request-token (str "Could not get request-token. " e)))))

(defn approval-url [request-token]
  (try
    (result/result (oauth/user-approval-uri consumer (:oauth_token request-token)))
    (catch Exception e (result/error {:request-token request-token} :approval-url (str "Could not get approval-url. " e)))))

(defn access-token-secret [request-token verifier]
  (try
    (result/result (oauth/access-token consumer request-token verifier))
    (catch Exception e (result/error {:request-token request-token :verifier verifier} :access-token-secret (str "Could not get access-token-secret. " e)))))


(def credentials (make-oauth-creds app-consumer-key
                   app-consumer-secret
                   user-access-token
                   user-access-token-secret))



; simply retrieves the user, authenticating with the above credentials
; note that anything in the :params map gets the -'s converted to _'s
;(users-show :oauth-creds my-creds :params {:screen-name "EmekaMosanya"})

; supplying a custom header
;(users-show :oauth-creds my-creds :params {:screen-name "AdamJWynne"} :headers {:x-blah-blah "value"})

; shows the users friends
;(friendships-show :oauth-creds my-creds
;  :params {:screen-name "EmekaMosanya"})

; use a custom callback function that only returns the body of the response
;(friendships-show :oauth-creds my-creds
;  :callbacks (SyncSingleCallback. response-return-body
;               response-throw-error
;               exception-rethrow)
;  :params {:screen-name "EmekaMosanya"})


; upload a picture tweet with a text status attached, using the default sync-single callback
;(statuses-update-with-media :oauth-creds *creds*
;  :body [(file-body-part "/pics/test.jpg")
;         (status-body-part "testing")])