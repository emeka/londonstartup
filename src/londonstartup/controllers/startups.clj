(ns londonstartup.controllers.startups
  (:require [ring.util.response :as resp]
            [londonstartup.models.startup :as startup]
            [londonstartup.views.startups :as views]
            [londonstartup.common.result :as result])
  (import org.bson.types.ObjectId))

(defn adapt-startup [startup]
  (if-let [_id (:_id startup)]
    (if (instance? ObjectId _id)
      startup
      (try
        (assoc startup :_id (ObjectId. _id))
        (catch Exception e (dissoc startup :_id))))
    startup))

(defn startups [& new-startup]
  (views/startups-page (first new-startup) (result/value (startup/startups))))

(defn startup [website]
  (let [lookup-result (startup/website->startup website)]
    (if (not (result/has-error? lookup-result))
      (views/startup-page (result/value lookup-result))
      (resp/redirect "/startups")
      )))

(defn startup-new [startup]
  (let [startup (adapt-startup startup)
        add-result (startup/add! startup)]
    (if (not (result/has-error? add-result))
      (resp/redirect-after-post (str "/startup/" (:website startup)))
      (startups startup))))

(defn startup-update [startup]
  (let [startup (adapt-startup startup)
        update-result (startup/update! startup)]
    (if (not (result/has-error? update-result))
      (resp/redirect-after-post (str "/startup/" (:website startup)))
      (startup startup))))

(defn startup-delete [website]
  (startup/remove-website! website)
  (resp/redirect "/startups"))

;;Forms

(defn add-startup-form []
  (views/add-startup-page))

(defn update-startup-form [website]
  (let [lookup-result (startup/website->startup website)]
    (if (not (result/has-error? lookup-result))
      (views/update-startup-page (result/value lookup-result))
      (resp/redirect "/startups")
      )))
