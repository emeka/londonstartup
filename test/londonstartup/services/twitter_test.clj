(ns londonstartup.services.twitter-test
  (:require [londonstartup.services.twitter :as t]
            [londonstartup.common.result :as result])
  (:use [clojure.test]
        [twitter.oauth]
        [twitter.callbacks]
        [twitter.callbacks.handlers]
        [twitter.api.restful]))

; simply retrieves the user, authenticating with the above credentials
; note that anything in the :params map gets the -'s converted to _'s

;(deftest user-show
;  (is (= "" (users-show :oauth-creds twitter/credentials :params {:screen-name "lastfm"}))))

(deftest authentication-consumer
  (is (not (nil? t/authentication-consumer))))

(deftest request-token
  (is (not (result/has-error? (t/request-token "http://staging.startupdirectory.org/login")))))

(deftest approval-url
  (let [request-token (result/value (t/request-token "http://staging.startupdirectory.org/login"))
        url (result/value (t/approval-url request-token))]
    (is (= (str "http://api.twitter.com/oauth/authorize?oauth_token=" (:oauth_token request-token)) url))))