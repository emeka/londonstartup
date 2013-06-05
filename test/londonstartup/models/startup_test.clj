(ns londonstartup.models.startup-test
  (:require [londonstartup.models.startup :as startup]
            [monger.core :as mg]
            [monger.collection :as mc])
  (:use clojure.test)
  (:use noir.util.test)
  (import org.bson.types.ObjectId))


;; Result test
(deftest add-error
  (let [init-result (startup/result nil)
        result1 (startup/add-error init-result :website "Error1")
        result2 (startup/add-error result1 :website "Error2")
        result3 (startup/add-error result2 :name "Name Error")]
    (is (= {:value nil :errors {:website ["Error1"]}} result1))
    (is (= {:value nil :errors {:website ["Error1" "Error2"]}} result2))
    (is (= {:value nil :errors {:website ["Error1" "Error2"] :name ["Name Error"]}} result3))))

(deftest result
  (is (= {:value nil} (startup/result nil)) )
  (is (= {:value 3} (startup/result 3))))

(deftest error
  (is (= {:errors {:website ["Error"]}} (startup/error :website "Error"))))

(deftest has-error?
  (is (not (startup/has-error? (startup/result nil))))
  (is (startup/has-error? (startup/add-error (startup/result nil) :website "Error"))))

(deftest errors
  (is (nil? (startup/errors (startup/result nil))))
  (is (= {:website ["Error"]} (startup/errors (startup/add-error (startup/result nil) :website "Error")))))

(deftest value
  (is (nil? (startup/value (startup/result nil))))
  (is (= 3 (startup/value (startup/result 3)))))

;; CRUD test
(let [google-id (ObjectId.)
      yahoo-id (ObjectId.)
      github-id (ObjectId.)
      google {:_id google-id :website "www.google.com" :name "Google Inc."}
      yahoo {:_id yahoo-id :website "www.yahoo.com" :name "Yahoo! Inc."}
      github {:_id github-id :website "www.github.com" :name "Github"}]

;; Fixtures
  (defn init-db [f]
    (mg/connect!)
    (mg/set-db! (mg/get-db "londonstartuptest"))
    (f))

  (defn clean-db [f]
    (mc/remove "startups")
    (startup/add! google)
    (startup/add! yahoo)
    (f))

  (use-fixtures :once init-db)
  (use-fixtures :each clean-db)

;;Tests
  (deftest valid?
      (is (not (startup/has-error? (startup/valid? google))))
      (is (startup/has-error? (startup/valid? {}))))

  (deftest total
    (is (= 2 (startup/value (startup/total)))))

  (deftest id->startup
    (is (= google (startup/value (startup/id->startup google-id)))))

  (deftest website->startup
    (is (= google (startup/value (startup/website->startup "www.google.com")))))

  (deftest website-free?
    (is (startup/value (startup/website-free? "www.doesnotexist.com")))
    (is (not (startup/value (startup/website-free? "www.google.com")))))

  (deftest startups
    (is (= (list google yahoo) (startup/value (startup/startups)))))

  (deftest add!
    (startup/add! github)
    (is (= 3 (startup/value (startup/total)))))

  (deftest update!
    (is (= google-id (startup/value (startup/update! (merge google {:website "www.new.com"})))))
    ;(is (= "www.new.com" (:website (startup/value (startup/id->startup google-id))))))
    )

  (deftest remove!
    (startup/remove! google-id)
    (is (= 1 (startup/value (startup/total))))
    (is (= nil (startup/value (startup/id->startup google-id)))))

  (deftest remove-website!
    (startup/remove-website! "www.google.com")
    (is (= 1 (startup/value (startup/total))))
    (is (= nil (startup/value (startup/id->startup google-id))))))
