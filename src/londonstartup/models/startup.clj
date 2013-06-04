(ns londonstartup.models.startup
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [noir.validation :as validate])
  (import org.bson.types.ObjectId))


(defn has-id [startup]
  (let [id (:_id startup)]
  (and id (not-empty id))))

(let [collection "startups"]
  (defn total []
    (mc/count collection))

  (defn id->startup [id]
    (mc/find-one-as-map collection {:_id id}))

  (defn website->startup [website]
    (mc/find-one-as-map collection {:website website}))

  (defn startups []
    (mc/find-maps collection))

  (defn valid? [{:keys [website name]}]
    (validate/rule (validate/has-value? website)
      [:website "A startup must have a website"])
    (validate/rule (validate/has-value? name)
      [:name "A startup must have a name"])
    ;(not (validate/errors? :website :name)
    )

  (defn add! [startup]
    (when (valid? startup)
      (let [oid (if  (has-id startup) (:_id startup) (ObjectId.))]
        (if (nil? (id->startup oid))
          (get (mc/insert-and-return collection (merge startup {:_id oid})) :_id )))))

  (defn update! [startup]
    (when (valid? startup)
      (if-let [oid (:_id startup)]
        (if (not (nil? (id->startup oid)))
          (do
            (mc/update-by-id collection oid startup)
            oid)))))

  (defn remove! [id]
    (mc/remove-by-id collection id))

  (defn remove-website! [website]
    (mc/remove collection {:website website}))

  )