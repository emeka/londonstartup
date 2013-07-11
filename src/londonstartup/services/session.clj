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

(defn clear! []
  (ns/clear!))

(defn flash! [message]
  (ns/flash-put! :message message))

(defn flash []
  (ns/flash-get :message))
