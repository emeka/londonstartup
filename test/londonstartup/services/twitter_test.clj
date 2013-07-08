(ns londonstartup.services.twitter-test
  (:require [londonstartup.services.twitter :as twitter])
  (:use [clojure.test]
        [twitter.oauth]
        [twitter.callbacks]
        [twitter.callbacks.handlers]
        [twitter.api.restful]))

; simply retrieves the user, authenticating with the above credentials
; note that anything in the :params map gets the -'s converted to _'s

(deftest user-show
  (is (= "" (users-show :oauth-creds twitter/credentials :params {:screen-name "lastfm"}))))