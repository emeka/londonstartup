(ns londonstartup.models.startup
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [noir.validation :as validate])
  (import org.bson.types.ObjectId))

;; Gets

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
      (let [oid (if (contains? startup :_id ) (get startup :_id ) (ObjectId.))]
        (if (nil? (id->startup oid))
          (get (mc/insert-and-return collection (merge startup {:_id oid})) :_id )))))

  (defn edit! [startup]
    (when (valid? startup)
      (let [oid (get startup :_id )]
        (if (not (nil? oid))
          (if (not (nil? (id->startup oid)))
            (mc/update-by-id collection oid startup))))))

  (defn remove! [id]
    (mc/remove-by-id collection id))

  )