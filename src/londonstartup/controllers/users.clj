(ns londonstartup.controllers.users
  (:require [ring.util.response :as resp]
            [londonstartup.models.users :as users]
            [londonstartup.views.users :as views]
            [londonstartup.common.result :as result]
            [londonstartup.services.session :as session])
  (import org.bson.types.ObjectId))

(defn set-id [user]
  (if-let [_id (:_id user)]
    (if (instance? ObjectId _id)
      user
      (try
        (assoc user :_id (ObjectId. _id))
        (catch Exception e (dissoc user :_id ))))
    user))

;This is to avoid injecting addition user fields like :auth for example
(def fields [:_id :username :githubAccount :linkdedInAccount :googlePlusAccount])
(defn sanetize [user]
  (let [user (select-keys user fields)]
    (set-id user)))

;;Read
(defn users [query]
  (views/users-page (users/users) query))

(defn user [username]
  (let [lookup-result (users/username->user username)]
    (if (not (result/has-error? lookup-result))
      (views/user-page lookup-result)
      (resp/redirect "/users"))))

;;Modify
(defn user-new [user uri]
  (let [user (sanetize user)
        user (merge user (session/user))
        add-result (users/add! user)]
    (if (not (result/has-error? add-result))
      (resp/redirect-after-post (str "/users/" (:username user)))
      (views/signup-page add-result uri))))

(defn user-update [user & [default-username]]
  (let [user (sanetize user)
        update-result (users/update! user)]
    (if (not (result/has-error? update-result))
      (resp/redirect-after-post (str "/users/" (:username user)))
      (views/update-user-page update-result default-username))))

(defn user-delete [username]
  (users/remove-username! username)
  (resp/redirect "/users"))

;;Forms
(defn signup [uri]
  (views/signup-page (result/result (session/user)) uri))

(defn update-user-form [username]
  (let [lookup-result (users/username->user username)]
    (if (not (result/has-error? lookup-result))
      (views/update-user-page lookup-result)
      (resp/redirect "/users"))))