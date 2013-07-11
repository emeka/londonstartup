(ns londonstartup.models.users-test
  (:require [londonstartup.models.users :as users]
            [londonstartup.models :as models]
            [monger.core :as mg]
            [monger.collection :as mc]
            [londonstartup.common.result :as result])
  (:use clojure.test)
  (import org.bson.types.ObjectId))




;; CRUD test
(let [user1-id (ObjectId.)
      user2-id (ObjectId.)
      user3-id (ObjectId.)
      user1 {:_id user1-id :username "User1" :auth {:twitter {}}}
      user2 {:_id user2-id :username "User2" :auth {:twitter {}}}
      user3 {:_id user3-id :username "User3" :auth {:twitter {}}}]

  ;; Fixtures
  (defn init-db [f]
    (models/initialize)
    (binding [users/collection "usersTEST"]
      (f)))

  (defn clean-db [f]
    (mc/remove users/collection)
    (users/add! user1)
    (users/add! user2)
    (f))

  (use-fixtures :once init-db)
  (use-fixtures :each clean-db)

     ;;Tests
  (deftest valid?
    (is (not (result/has-error? (users/valid? user1))))
    (is (result/has-error? (users/valid? {}))))

  (deftest total
    (is (= 2 (result/value (users/total)))))

  (deftest id->user
    (is (= user1 (result/value (users/id->user user1-id))))
    (is (result/has-error? (users/id->user "1234"))))

  (deftest username-free?
    (is (not (result/has-error? (users/username-free? {:username "Foo"}))))
    (is (result/has-error? (users/username-free? {:username "User1"}))))

  (deftest id-free?
    (is (not (result/has-error? (users/id-free? {}))))
    (is (not (result/has-error? (users/id-free? {:_id (ObjectId.)}))))
    (is (result/has-error? (users/id-free? {:_id user1-id}))))

  (deftest users
    (is (= (list user1 user2) (result/value (users/users))))
    (is (= (list user1) (result/value (users/users {"_id" user1-id})))))

  (deftest add!
    (users/add! user3)
    (is (= 3 (result/value (users/total))))
    (is (result/has-error? (users/add! user3)))
    (is (= 3 (result/value (users/total))))
    (is (result/has-error? (users/add! {:username "User1"})))
    (is (= 3 (result/value (users/total)))))

  (deftest update!
    (let [update-result (users/update! (merge user1 {:username "New"}))]
      (is (nil? (result/errors update-result)))
      (is (= user1-id (:_id (result/value update-result)))))
    (is (= "New" (:username (result/value (users/id->user user1-id)))))
    (is (result/has-error? (users/update! (merge user1 {:username "User2"}))))
    )

  (deftest remove!
    (users/remove! user1-id)
    (is (= 1 (result/value (users/total))))
    (is (= nil (result/value (users/id->user user1-id)))))

  (deftest remove-username!
    (users/remove-username! "User1")
    (is (= 1 (result/value (users/total))))
    (is (= nil (result/value (users/id->user user1-id))))))

