(ns londonstartup.models.startup-test
  (:require [londonstartup.models.startup :as startup]
            [londonstartup.models :as models]
            [monger.core :as mg]
            [monger.collection :as mc]
            [londonstartup.common.result :as result])
  (:use clojure.test)
  (import org.bson.types.ObjectId))

;; CRUD test
(let [google-id (ObjectId.)
      yahoo-id (ObjectId.)
      github-id (ObjectId.)
      google {:_id google-id :website "www.google.com" :name "Google Inc." :lead "A leading search engine."}
      yahoo {:_id yahoo-id :website "www.yahoo.com" :name "Yahoo! Inc." :lead "A leading directory."}
      github {:_id github-id :website "www.github.com" :name "Github" :lead "The open source repository."}]

  ;; Fixtures
  (defn init-db [f]
    (models/initialize)
    (binding [startup/collection "startupsTEST"]
      (f)))

  (defn clean-db [f]
    (mc/remove startup/collection)
    (startup/add! google)
    (startup/add! yahoo)
    (f))

  (use-fixtures :once init-db)
  (use-fixtures :each clean-db)

  ;;Tests
  (deftest valid?
    (is (not (result/has-error? (startup/valid? google))))
    (is (result/has-error? (startup/valid? {}))))

  (deftest total
    (is (= 2 (result/value (startup/total)))))

  (deftest id->startup
    (is (= google (result/value (startup/id->startup google-id))))
    (is (result/has-error? (startup/id->startup "1234"))))

  (deftest website->startup
    (is (= google (result/value (startup/website->startup "www.google.com"))))
    (is (result/has-error? (startup/website->startup "www.doesnotexist.com"))))

  (deftest website-free?
    (is (not (result/has-error? (startup/website-free? {:website "www.doesnotexist.com"}))))
    (is (result/has-error? (startup/website-free? {:website "www.google.com"}))))

  (deftest name-free?
    (is (not (result/has-error? (startup/name-free? {:name "New Inc."}))))
    (is (result/has-error? (startup/name-free? {:name "Google Inc."}))))

  (deftest id-free?
    (is (not (result/has-error? (startup/id-free? {}))))
    (is (not (result/has-error? (startup/id-free? {:_id (ObjectId.)}))))
    (is (result/has-error? (startup/id-free? {:_id google-id}))))

  (deftest startups
    (is (= (list google yahoo) (result/value (startup/startups)))))

  (deftest add!
    (startup/add! github)
    (is (= 3 (result/value (startup/total))))
    (is (result/has-error? (startup/add! github)))
    (is (= 3 (result/value (startup/total))))
    (is (result/has-error? (startup/add! {:website "www.github.com" :name "Other"})))
    (is (= 3 (result/value (startup/total))))
    (is (result/has-error? (startup/add! {:website "www.other.com" :name "Github"})))
    (is (= 3 (result/value (startup/total)))))

  (deftest update!
    (let [update-result (startup/update! (merge google {:website "www.new.com"}))]
      (is (nil? (result/errors update-result)))
      (is (= google-id (:_id (result/value update-result)))))
    (is (= "www.new.com" (:website (result/value (startup/id->startup google-id)))))
    (is (result/has-error? (startup/update! (merge google {:website "www.yahoo.com"}))))
    (is (result/has-error? (startup/update! (merge google {:name "Yahoo! Inc."}))))
    )

  (deftest remove!
    (startup/remove! google-id)
    (is (= 1 (result/value (startup/total))))
    (is (= nil (result/value (startup/id->startup google-id)))))

  (deftest remove-website!
    (startup/remove-website! "www.google.com")
    (is (= 1 (result/value (startup/total))))
    (is (= nil (result/value (startup/id->startup google-id))))))
