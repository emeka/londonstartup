(ns londonstartup.models.startup-test
  (:require [londonstartup.models.startup :as startup]
            [londonstartup.models :as models]
            [monger.core :as mg]
            [monger.collection :as mc]
            [londonstartup.common.result :as result])
  (:use clojure.test)
  (import org.bson.types.ObjectId))


;; Result test
(deftest add-error
  (let [init-result (result/result nil)
        result1 (result/add-error init-result :website "Error1")
        result2 (result/add-error result1 :website "Error2")
        result3 (result/add-error result2 :name "Name Error")]
    (is (= {:value nil :errors {:website ["Error1"]}} result1))
    (is (= {:value nil :errors {:website ["Error1" "Error2"]}} result2))
    (is (= {:value nil :errors {:website ["Error1" "Error2"] :name ["Name Error"]}} result3))))

(deftest result
  (is (= {:value nil} (result/result nil)))
  (is (= {:value 3} (result/result 3))))

(deftest error
  (is (= {:errors {:website ["Error"]}} (result/error :website "Error"))))

(deftest has-error?
  (is (not (result/has-error? (result/result nil))))
  (is (result/has-error? (result/add-error (result/result nil) :website "Error"))))

(deftest errors
  (is (nil? (result/errors (result/result nil))))
  (is (= {:website ["Error"]} (result/errors (result/add-error (result/result nil) :website "Error")))))

(deftest value
  (is (nil? (result/value (result/result nil))))
  (is (= 3 (result/value (result/result 3)))))


;; CRUD test
(let [google-id (ObjectId.)
      yahoo-id (ObjectId.)
      github-id (ObjectId.)
      google {:_id google-id :website "www.google.com" :name "Google Inc."}
      yahoo {:_id yahoo-id :website "www.yahoo.com" :name "Yahoo! Inc."}
      github {:_id github-id :website "www.github.com" :name "Github"}]

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
    (is (= google (result/value (startup/id->startup google-id)))))

  (deftest website->startup
    (is (= google (result/value (startup/website->startup "www.google.com")))))

  (deftest website-free?
    (is (result/value (startup/website-free? "www.doesnotexist.com")))
    (is (not (result/value (startup/website-free? "www.google.com")))))

  (deftest name-free?
    (is (result/value (startup/name-free? "New Inc.")))
    (is (not (result/value (startup/name-free? "Google Inc.")))))

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
    (is (= google-id (result/value (startup/update! (merge google {:website "www.new.com"})))))
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
