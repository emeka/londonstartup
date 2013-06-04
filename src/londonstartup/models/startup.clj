(ns londonstartup.models.startup
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [noir.validation :as validate])
  (import org.bson.types.ObjectId))


(defn has-id [startup]
  (let [id (:_id startup)]
    (and id (not-empty (str id)))))

(defn valid? [{:keys [website name]}]
  (validate/rule (validate/has-value? website)
    [:website "A startup must have a website"])
  (validate/rule (validate/has-value? name)
    [:name "A startup must have a name"])
  ;(not (validate/errors? :website :name)) ;;TODO: valid? should be at view level.
  )

(let [collection "startups"]
  (defn total []
    (mc/count collection))

  (defn id->startup [id]
    (mc/find-one-as-map collection {:_id id}))

  (defn website->startup [website]
    (mc/find-one-as-map collection {:website website}))

  (defn website-free? [website]
    (= 0 (mc/count collection {:website website})))

  (defn startups []
    (mc/find-maps collection))

  (defn add! [startup]
    (when (valid? startup)
      (let [oid (if (has-id startup) (:_id startup) (ObjectId.))]
        (if (and (nil? (id->startup oid)) (website-free? (:website startup)))
          (get (mc/insert-and-return collection (merge startup {:_id oid})) :_id )))))

  (defn update! [startup]
    (when (valid? startup)
      (when-let [id (:_id startup)]
        (when-let [old-startup (id->startup id)]
          (if (or (= (:website startup) (:website old-startup)) (website-free? (:website startup)))
            (do
              (mc/update-by-id collection id startup)
              id))))))

  (defn remove! [id]
    (mc/remove-by-id collection id))

  (defn remove-website! [website]
    (mc/remove collection {:website website}))

  )