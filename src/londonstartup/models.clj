(ns londonstartup.models
  (:require [monger.core :as mg]))


(defn initialize []
  (let [uri (get (System/getenv) "MONGOLAB_URI" "mongodb://127.0.0.1/londonstartup")]
    (monger.core/connect-via-uri! uri)))