(ns londonstartup.models
  (:require [monger.core :as mg]
            [londonstartup.environment :as env]))


(defn db-connected? []
  (bound? #'mg/*mongodb-connection*))

(defn db-connect []
  (if (not (db-connected?))
    (let [uri (env/get "MONGOLAB_URI" (env/get "MONGOHQ_URL_STARTUP_DIRECTORY" "mongodb://127.0.0.1/londonstartup"))]
      (monger.core/connect-via-uri! uri))))

