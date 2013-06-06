(ns londonstartup.controllers.startups
  (:require [ring.util.response :as resp]
            [londonstartup.models.startup :as startup]
            [londonstartup.views.startups :as views]
            [londonstartup.common.result :as result])
  (import org.bson.types.ObjectId))

;; Routing
(defn startups []
  (views/startups-page (result/value (startup/startups))))

(defn startup [website]
  (views/startup-page (result/value (startup/website->startup website))))

(defn startup-new [website new-startup]
  (if (not (result/has-error? (startup/add! new-startup)))
    (resp/redirect (str "/startup/" website))
    (startups)))

(defn startup-update [website _id updated-startup]
  (if (not (result/has-error? (startup/update! (dissoc (merge updated-startup {:_id (ObjectId. _id)}) :_method, :_website))))
    (resp/redirect (str "/startup/" website))
    (startup updated-startup)))

(defn startup-delete [website]
  (startup/remove-website! website)
  (resp/redirect "/startups"))