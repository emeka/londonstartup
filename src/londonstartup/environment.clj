(ns londonstartup.environment
  (:refer-clojure :exclude [get]))


(defn get
  ([key] (get key nil))
  ([key default] (clojure.core/get (System/getenv) key default)))

(def debug? (= "true" (get "DEBUG")))
