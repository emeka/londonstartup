(ns londonstartup.models.startup
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [londonstartup.common.result :as result]
            [londonstartup.common.validation :as validate])
  (import org.bson.types.ObjectId))


(def ^:dynamic collection "startups")

;; Validation
(defn has-id? [startup]
  (let [id (:_id startup)]
    (and id (not-empty (str id)))))

(defn valid? [startup]
  (result/merge-error-> startup
    (validate/has-value? :website "A startup must have a website")
    (validate/has-value? :name "A startup must have a name")))

;; CRUD

(defn total []
  (result/result (mc/count collection)))

(defn id->startup [id]
  (let [lookup (mc/find-one-as-map collection {:_id id})]
    (if lookup
      (result/result lookup)
      (result/error lookup :website "Unknown startup ID"))))

(defn website->startup [website]
  (let [lookup (mc/find-one-as-map collection {:website website})]
    (if lookup
      (result/result lookup)
      (result/error lookup :website "Unknown website"))))

(defn website-free? [{:keys [website] :as startup}]
  (if (= 0 (mc/count collection {:website website}))
    (result/result startup)
    (result/error startup :website "The website already exists")))

(defn name-free? [{:keys [name] :as startup}]
  (if (= 0 (mc/count collection {:name name}))
    (result/result startup)
    (result/error startup :name "The name already exists")))

(defn id-free? [{:keys [_id] :as startup}]
  (if (or (not _id) (result/has-error? (id->startup _id)))
    (result/result startup)
    (result/error startup :_id "Startup entry already exists")))

(defn startups []
  (result/result (mc/find-maps collection)))

(defn convert-id [startup]
  (if-let [_id (:_id startup)]
    (if (instance? ObjectId _id)
      (result/result startup)
      (try
        (result/result (assoc startup :_id (ObjectId. _id)))
        (catch Exception e (dissoc startup :_id))))
    (result/result startup)))

(defn generate-id-if-needed [startup]
  (if (has-id? startup)
    (result/result startup)
    (result/result (merge startup {:_id (ObjectId.)}))))

(defn- add-raw! [startup]
  (mc/insert-and-return collection startup))

(defn add! [startup]
  (result/until-error-> startup
    (convert-id)
    (valid?)
    (id-free?)
    (generate-id-if-needed)
    (website-free?)
    (name-free?)
    (add-raw!)))

(defn- update-raw! [startup]
  (try
    (do
      (let [update-result (mc/update-by-id collection (:_id startup) startup)]
        (if (.getField update-result "updatedExisting")
          (result/result startup)
          (result/error startup :startup "Could not update"))))
    (catch Exception e (result/error :startup "Could not update"))))

(defn update-valid? [new-startup current-startup]
  (if (or (= (:website new-startup) (:website current-startup)) (not (result/has-error? (website-free? new-startup))))
    (if (or (= (:name new-startup) (:name current-startup)) (not (result/has-error? (name-free? new-startup))))
      (result/result new-startup)
      (result/error new-startup :name "Company name already in use."))
    (result/error new-startup :website "Website already in use.")))

(defn update! [startup]
  (result/until-error-> startup
    (convert-id)
    (valid?)
    (update-valid? (result/value (id->startup (:_id startup))))
    (update-raw!)))

(defn remove! [id]
  (result/result (mc/remove-by-id collection id)))

(defn remove-website! [website]
  (result/result (mc/remove collection {:website website})))

