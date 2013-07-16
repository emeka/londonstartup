(ns londonstartup.models
  (:require [monger.core :as mg]))


(defn db-connected? []
  (bound? #'mg/*mongodb-connection*))

(defn db-connect []
  (if (not (db-connected?))
    (let [uri (get (System/getenv) "MONGOLAB_URI"
                (get (System/getenv) "MONGOHQ_URL_STARTUP_DIRECTORY" "mongodb://127.0.0.1/londonstartup"))]
      (monger.core/connect-via-uri! uri))))

