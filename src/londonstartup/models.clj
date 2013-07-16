(ns londonstartup.models
  (:require [monger.core :as mg]))


(defn initialize []
  (let [uri (get (System/getenv) "MONGOLAB_URI"
              (get (System/getenv) "MONGOHQ_URL" "mongodb://127.0.0.1/londonstartup"))]
    (monger.core/connect-via-uri! uri)))