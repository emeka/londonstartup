(ns londonstartup.models.startup
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [noir.validation :as validate]
            [londonstartup.models :as models])
  (import org.bson.types.ObjectId))


(def ^:dynamic collection "startups")

;; Result
(defn add-error [result key msg]
  (let [errors (get-in result [:errors key] [])]
    (assoc-in result [:errors key] (conj errors msg))))

(defn result [value]
  {:value value})

(defn error [key msg]
  {:errors {key [msg]}})

(defn has-error? [result]
  (contains? result :errors ))

(defn errors [result]
  (:errors result))

(defn value [result]
  (:value result))

;; Validation
(defn has-id [startup]
  (let [id (:_id startup)]
    (and id (not-empty (str id)))))

(def validation-rules
  [[:website validate/has-value? "A startup must have a website"],
   [:name validate/has-value? "A startup must have a name"]])

(defn valid?
  ([startup]
    (reduce #(valid? startup %1 %2) (result startup) validation-rules))
  ([startup result [field test msg]]
    (if (test (field startup))
      result
      (add-error result field msg)
      )))

;; CRUD

(defn total []
  (result (mc/count collection)))

(defn id->startup [id]
  (result (mc/find-one-as-map collection {:_id id})))

(defn website->startup [website]
  (result (mc/find-one-as-map collection {:website website})))

(defn website-free? [website]
  (result (= 0 (mc/count collection {:website website}))))

(defn name-free? [name]
  (result (= 0 (mc/count collection {:name name}))))

(defn startups []
  (result (mc/find-maps collection)))

(defn add! [startup]
  (let [validation-result (valid? startup)]
    (if (not (has-error? validation-result))
      (let [oid (if (has-id startup) (:_id startup) (ObjectId.))]
        (if (nil? (value (id->startup oid)))
          (if (value (website-free? (:website startup)))
            (if (value (name-free? (:name startup)))
              (result (get (mc/insert-and-return collection (merge startup {:_id oid})) :_id ))
              (error :name "Company name already in use."))
            (error :website "Website already in use"))
          (error :startup "Startup already exists")))
      validation-result)))

(defn update! [startup]
  (let [validation-result (valid? startup)]
    (if (not (has-error? validation-result))
      (when-let [id (:_id startup)]
        (when-let [old-startup (value (id->startup id))]
          (if (or (= (:website startup) (:website old-startup)) (value (website-free? (:website startup))))
            (if (or (= (:name startup) (:name old-startup)) (value (name-free? (:name startup))))
              (do
                (mc/update-by-id collection id startup)
                (result id))
              (error :name "Company name already in use."))
            (error :website "Website already in use."))))
      validation-result)))


(defn remove! [id]
  (result (mc/remove-by-id collection id)))

(defn remove-website! [website]
  (result (mc/remove collection {:website website})))