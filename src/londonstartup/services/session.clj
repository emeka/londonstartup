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

(defn user-logged! [user]
  (put! :user user))

(defn user-logged? []
  (get :user))

(defn user-logout! []
  (remove! :user))

(defn user []
  (get :user))