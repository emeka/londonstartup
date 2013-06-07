(ns londonstartup.models.startup
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [noir.validation :as validate]
            [londonstartup.common.result :as result])
  (import org.bson.types.ObjectId))


(def ^:dynamic collection "startups")


;; Validation
(defn has-id [startup]
  (let [id (:_id startup)]
    (and id (not-empty (str id)))))

(def validation-rules
  [[:website validate/has-value? "A startup must have a website"],
   [:name validate/has-value? "A startup must have a name"]])

(defn valid?
  ([startup]
    (reduce #(valid? startup %1 %2) (result/result startup) validation-rules))
  ([startup result [field test msg]]
    (if (test (field startup))
      result
      (result/add-error result field msg)
      )))

;; CRUD

(defn total []
  (result/result (mc/count collection)))

(defn id->startup [id]
  (let [lookup (mc/find-one-as-map collection {:_id id})]
    (if lookup
      (result/result lookup)
      (result/error :website "Unknown startup ID"))))

(defn website->startup [website]
  (let [lookup (mc/find-one-as-map collection {:website website})]
    (if lookup
      (result/result lookup)
      (result/error :website "Unknown website"))))

(defn website-free? [website]
  (result/result (= 0 (mc/count collection {:website website}))))

(defn name-free? [name]
  (result/result (= 0 (mc/count collection {:name name}))))

(defn startups []
  (result/result (mc/find-maps collection)))

(defn add! [startup]
  (let [validation-result (valid? startup)]
    (if (not (result/has-error? validation-result))
      (let [oid (if (has-id startup) (:_id startup) (ObjectId.))]
        (if (nil? (result/value (id->startup oid)))
          (if (result/value (website-free? (:website startup)))
            (if (result/value (name-free? (:name startup)))
              (result/result (mc/insert-and-return collection (merge startup {:_id oid})))
              (result/error :name "Company name already in use."))
            (result/error :website "Website already in use"))
          (result/error :startup "Startup already exists")))
      validation-result)))

(defn update! [startup]
  (let [validation-result (valid? startup)]
    (if (not (result/has-error? validation-result))
      (when-let [id (:_id startup)]
        (when-let [old-startup (result/value (id->startup id))]
          (if (or (= (:website startup) (:website old-startup)) (result/value (website-free? (:website startup))))
            (if (or (= (:name startup) (:name old-startup)) (result/value (name-free? (:name startup))))
              (try (do
                     (let [update-result (mc/update-by-id collection id startup)]
                       (if (.getField update-result "updatedExisting")
                         (result/result startup)
                         (result/error :startup "Could not update"))))
                (catch Exception e (result/error :startup "Could not update")))
              (result/error :name "Company name already in use."))
            (result/error :website "Website already in use."))))
      validation-result)))


(defn remove! [id]
  (result/result (mc/remove-by-id collection id)))

(defn remove-website! [website]
  (result/result (mc/remove collection {:website website})))

