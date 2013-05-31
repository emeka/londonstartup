(ns londonstartup.models.startup-test
  (:require [londonstartup.models.startup :as startup]
            [monger.core :as mg]
            [monger.collection :as mc])
  (:use clojure.test)
  (:use noir.util.test)
  (import org.bson.types.ObjectId))


(let [google-id (ObjectId.)
      yahoo-id (ObjectId.)
      github-id (ObjectId.)
      google {:_id google-id :website "www.google.com" :name "Google Inc."}
      yahoo {:_id yahoo-id :website "www.yahoo.com" :name "Yahoo! Inc."}
      github {:_id github-id :website "www.github.com" :name "Github"}]

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

  (deftest total
    (is (= 2 (startup/total))))

  (deftest id->startup
    (is (= google (startup/id->startup google-id))))

  (deftest website->startup
    (is (= google (startup/website->startup "www.google.com"))))

  (deftest startups
    (is (= (list google yahoo) (startup/startups))))

  (deftest add!
    (startup/add! github)
    (is (= 3 (startup/total))))

  (deftest edit!
    (startup/edit! (merge google {:website "www.new.com"}))
    (is (= "www.new.com" (get (startup/id->startup google-id) :website))))

  (deftest remove!
    (startup/remove! google-id)
    (is (= 1 (startup/total)))
    (is (= nil (startup/id->startup google-id))))

  ;(deftest valid?
  ;  (with-noir
  ;    (is (not (startup/valid? {})))
  ;    (is (startup/valid? {:website "www.google.com" :name "Google Inc."}))))

  )