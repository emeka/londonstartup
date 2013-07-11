(ns londonstartup.services.session
  (:refer-clojure :exclude [get remove])
  (:require [noir.session :as ns]))


(defn put! [k v]
  (ns/put! k v))

(defn get! [k]
  (ns/get! k))

(defn get [k]
  (ns/get k))

(defn remove! [k]
  (ns/remove! k))

(defn user-login! [user]
  (put! :user user))

(defn user-logged? []
  (not (nil? (get :user ))))

(defn user-logout! []
  (remove! :user ))

(defn user []
  (get :user))

(defn username []
  (get-in (get :user ) [:auth :twitter :screen_name ]))

(defn flash! [message]
  (ns/flash-put! :message message))

(defn flash []
  (ns/flash-get :message))
