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

;This is to avoid injecting additional user fields like :auth for example
(def fields [:_id :username :githubAccount :linkedInAccount :googlePlusAccount ])
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
      ;TODO flash error message
      (resp/redirect "/users"))))

;;Modify
(defn user-new [user uri]
  (let [user (sanetize user)
        user (merge user (session/get :user ))
        add-result (users/add! user)]
    (if (not (result/has-error? add-result))
      (resp/redirect-after-post (str "/users/" (:username user)))
      (views/signup-page add-result uri))))

(defn user-delete [username]
  (users/remove-username! username)
  (resp/redirect "/users"))

(defn settings-update [user]
  )

;;Settings
(defn user-settings [username uri]
  (let [lookup-result (users/username->user username)]
    (if (not (result/has-error? lookup-result))
      (views/settings-page lookup-result uri)
      (resp/redirect "/users"))))

(defn user-settings-update [settings uri & [default-username]]
  (let [settings (sanetize settings)
        user (merge (session/get :user ) settings)
        update-result (users/update! user)]
    (if (not (result/has-error? update-result))
      (do
        (session/put! :user (result/value update-result))
        (resp/redirect-after-post (str "/users/" (:username user))))
      (views/settings-page update-result uri default-username))))

(defn settings []
  (let [username (:username (session/get :user ))]
    )
  )

(defn signup [uri]
  (views/signup-page (result/result (session/get :user )) uri))