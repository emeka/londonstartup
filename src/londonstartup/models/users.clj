(ns londonstartup.models.users
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [londonstartup.common.result :as result]
            [londonstartup.common.validation :as validate])
  (import org.bson.types.ObjectId))

(def ^:dynamic collection "users")

(defn init [authentication]
  (result/result {:_id (ObjectId.)
                  :username nil
                  :auth authentication}))

;; Validation
;; Validation
(defn has-id? [user]
  (let [id (:_id user)]
    (and id (not-empty (str id)))))

(defn valid? [user]
  (result/merge-error-> user
    ;;(validate/has-value? :auth "A user must be authenticated")
    (validate/has-value? :username "A user must have a username")))

;; CRUD

(defn total []
  (result/result (mc/count collection)))

(defn id->user [id]
  (let [lookup (mc/find-one-as-map collection {:_id id})]
    (if lookup
      (result/result lookup)
      (result/error lookup :website "Unknown User ID"))))

(defn username->user [username]
  (let [lookup (mc/find-one-as-map collection {:username username})]
    (if lookup
      (result/result lookup)
      (result/error lookup :username "Unknown username"))))

(defn username-free? [{:keys [username] :as user}]
  (if (= 0 (mc/count collection {:username username}))
    (result/result user)
    (result/error user :name "The name already exists")))

(defn id-free? [{:keys [_id] :as user}]
  (if (or (not _id) (result/has-error? (id->user _id)))
    (result/result user)
    (result/error user :_id "User entry already exists")))

(defn users []
  (result/result (mc/find-maps collection)))

(defn generate-id [user]
  (if (has-id? user)
    (result/result user)
    (result/result (merge user {:_id (ObjectId.)}))))

(defn- add-raw! [user]
  (mc/insert-and-return collection user))

(defn add! [user]
  (result/until-error-> user
    (valid?)
    (id-free?)
    (generate-id)
    (username-free?)
    (add-raw!)))

(defn- update-raw! [user]
  (try
    (do
      (let [update-result (mc/update-by-id collection (:_id user) user)]
        (if (.getField update-result "updatedExisting")
          (result/result user)
          (result/error user :user "Could not update"))))
    (catch Exception e (result/error :user "Could not update"))))

(defn update-valid? [new-user current-user]
  (if (or (= (:username new-user) (:username current-user)) (not (result/has-error? (username-free? new-user))))
    (result/result new-user)
    (result/error new-user :name "Username name already in use.")))

(defn update! [user]
  (result/until-error-> user
    (valid?)
    (update-valid? (result/value (id->user (:_id user))))
    (update-raw!)))

(defn remove! [id]
  (result/result (mc/remove-by-id collection id)))

(defn remove-username! [username]
  (result/result (mc/remove collection {:username username})))
