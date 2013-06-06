(ns londonstartup.common.result )

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