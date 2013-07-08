(ns londonstartup.services.twitter
  (:require [oauth.client :as oauth])
  (:use [twitter.oauth]
        [twitter.callbacks]
        [twitter.callbacks.handlers]
        [twitter.api.restful])
  (:import (twitter.callbacks.protocols SyncSingleCallback)))


(def app-consumer-key "V4pHVpupNNtj0GYwcglzQ")
(def app-consumer-secret (get (System/getenv) "TWITTER_APP_CONSUMER_SECRET"))
(def user-access-token "19400021-Aqbp1v1MmxMGiPEI8qUhOEfVHAGsoIXNXs8yGCvF4")
(def user-access-token-secret (get (System/getenv) "TWITTER_ACCESS_TOKEN_SECRET"))


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