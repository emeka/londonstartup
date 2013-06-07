(ns londonstartup.handler-test
  (:require [londonstartup.controllers.startups :as startups])
  (:use clojure.test)
  (import org.bson.types.ObjectId))


(deftest adapt-startup
  (is (instance? ObjectId (:_id (startups/adapt-startup {:_id "51ae0bfe300497063eabcfc5"}))))
  (is (instance? ObjectId (:_id (startups/adapt-startup {:_id (ObjectId. "51ae0bfe300497063eabcfc5")}))))
  (is (nil? (:_id (startups/adapt-startup {:_id ""})))))