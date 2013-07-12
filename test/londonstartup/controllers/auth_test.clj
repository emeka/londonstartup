(ns londonstartup.controllers.auth-test
  (:require [londonstartup.controllers.auth :as auth]
            [londonstartup.environment :as env]
            [londonstartup.models.users :as users]
            [londonstartup.common.result :as result]
            [londonstartup.services.twitter :as twitter]
            [londonstartup.services.session :as session]
            [ring.util.response :as resp])

  (:use midje.sweet)
  (:use [midje.util :only [expose-testables]]))

(expose-testables londonstartup.controllers.auth)

(defn valid-result? [fact]
  (result/error-free? fact))

(defn error-result? [fact]
  (result/has-error? fact))

(defn result-value [value]
  (fn [fact] (= (result/value fact) value)))

(facts "Callback URL should be calculated from the environment."
  (against-background
    (env/get "BASE_URL" anything) => "http://baseurl.com",
    (env/get "URL") => anything)
  (fact "If BASE_URL or URL are given, it should return a result"
    (result/value (callback "foo")) => "http://baseurl.com/login?auto=true&redirect_to=foo")
  (fact "The redirect_to url is empty, it should redirect to '/'"
    (result/value (callback nil)) => "http://baseurl.com/login?auto=true&redirect_to=/")
  (fact "If BASE_URL and URL are empty, it should throw an exception"
    (callback "/") => (fn [actual] (result/has-error? actual))
    (provided (env/get "BASE_URL" anything) => nil)))

(facts "We should be able to initialise a user from a Twitter authorisation"
  (twitter-auth->user {:screen_name "Foo"}) => (result-value {:username "Foo" :auth {:twitter {:screen_name "Foo"}}}))

(facts "We should be able to find a user in the database from a session user"
  (fact "We should extract the Twitter user_id and search for it"
    (find-user {:auth {:twitter {:user_id "1" :foo "Foo"}}}) => ...User...
    (provided (users/user {"auth.twitter.user_id" "1"}) => ...User... :times 1))
  )

(facts "We should update the user in the database if the twitter authorisation changed"
  (fact "We should do nothing if the Twitter authorisations are the same"
    (update-user! {:auth {:twitter "Foo"}} {:auth {:twitter "Foo"}}) => anything
    (provided (users/update! anything) => anything :times 0))
  (fact "We should update if the Twitter authorisations are different"
    (update-user! {:auth {:twitter {:user_id "1" :foo "Foo"}}}
      {:auth {:twitter {:user_id "1" :foo "Bar"}}}) => ...Result...
    (provided (users/update! anything) => ...Result... :times 1))
  (fact "We should return an error if the user_id values are different"
    (update-user! {:auth {:twitter {:user_id "1"}}} {:auth {:twitter {:user_id "2"}}}) => error-result?
    (provided (users/update! anything) => anything :times 0)))

(facts "We should be able to get a request token for a callback url"
  (fact "We shoud return a valid result if we can calculate the callback and twitter request worked"
    (get-request-token "/") => valid-result?
    (provided
      (#'londonstartup.controllers.auth/callback "/") => (result/result "callback"),
      (twitter/request-token "callback") => (result/result "token")))
  (fact "We should return an error if we cannot calculate the callback"
    (get-request-token "/") => error-result?
    (provided
      (#'londonstartup.controllers.auth/callback "/") => (result/error "callback" :callback "Msg")))
  (fact "We should return an error if Twitter returns an error"
    (get-request-token "/") => error-result?
    (provided
      (#'londonstartup.controllers.auth/callback "/") => (result/result "callback"),
      (twitter/request-token "callback") => (result/error "token" :token "Msg"))))

(facts "Twitter request-token must be confirmed"
  (fact "We should return a valid result if oath_callback_confirmed is \"true\""
    (confirmed? {:oauth_callback_confirmed "true"}) => valid-result?)
  (fact "We should return an error result if oath_callback_confirmed is not \"true\""
    (confirmed? {:oauth_callback_confirmed "foo"}) => error-result?)
  (fact "We should return an error result if no oauth_callback_confirmed is given"
    (confirmed? {}) => error-result?))

(facts "Adding a value in the session should just return the value as a valid result"
  (session-put! ..Value.. ..Key..) => (result/result ..Value..)
  (provided (session/put! ..Key.. ..Value..) => anything :times 1))

(facts "Getting a value from the session should just return the value as a valid result"
  (session-get ..Key..) => (result/result ..Value..)
  (provided (session/get ..Key..) => ..Value.. :times 1))

(facts "We should be able to redirect the user to the Twitter authorisation page"
  (fact "If we can get the approval URL we should redirect to it"
    (redirect-to-twitter-auth-page ..URL..) => ..Redirect..
    (provided
      (#'londonstartup.controllers.auth/get-approval-url ..URL..) => (result/result ..Approval-URL..),
      (resp/redirect ..Approval-URL..) => ..Redirect.. :times 1))
  (fact "If we cannot get the approval URL we should not redirect"
    (redirect-to-twitter-auth-page ..URL..) => anything
    (provided
      (#'londonstartup.controllers.auth/get-approval-url ..URL..) => (result/error ..Approval-URL.. :foo "Msg"),
      (resp/redirect ..Approval-URL..) => ..Redirect.. :times 0)))

(facts "We should be able to log existing users"
  (fact "If we can update the user in the database, we should save them in the session and redirect to the given url"
    (log-existing-user ..db-user.. ..session-user.. ..redirect-url..) => ..Redirect..
    (provided
      (#'londonstartup.controllers.auth/update-user! ..db-user.. ..session-user..) => (result/result ..added-user..),
      (session/put! :user ..added-user..) => anything :time 1,
      (resp/redirect ..redirect-url..) => ..Redirect.. :times 1)))