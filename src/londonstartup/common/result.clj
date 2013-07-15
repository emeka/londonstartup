(ns londonstartup.common.result)

;from the old clojure.contrib.map-utils
(defn- deep-merge-with
  "Like merge-with, but merges maps recursively, applying the given fn
  only when there's a non-map at a particular level.

  (deepmerge + {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
               {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
  -> {:a {:b {:z 3, :c 3, :d {:z 9, :x 1, :y 2}}, :e 103}, :f 4}"
  [f & maps]
  (apply
    (fn m [& maps]
      (if (every? map? maps)
        (apply merge-with m maps)
        (apply f maps)))
    maps))


;; Result

(defn result? [result]
  (and (map? result) (contains? result :value )))

(defn result [value]
  (if (result? value)
    value
    {:value value}))

(defn add-error
  ([value key msg]
    (add-error value {:errors {key [msg]}}))
  ([value errors]
    (deep-merge-with (fn [& msgs] (vec (flatten (concat msgs)))) (result value) (select-keys errors [:errors]))))

(defn error [value key msg]
  (add-error value key msg))

(defn has-error? [value]
  (and (result? value ) (contains? (result value) :errors )))

(defn error-free? [value]
  (and (result? value ) (not (has-error? value))))

(defn errors [value]
  (:errors value))

(defn value [value]
  (:value value))

(defmacro merge-error->
  "Threads the results through the forms while merging the errors.
  Inserts the result value as the
  second item in the first form, making a list of it if it is not a
  list already. If there are more forms, inserts the first form as the
  second item in second form, etc."
  {:added "1.0"}
  ([x] `(result ~x))
  ([x form]
    (let [local-x (gensym "local")
          local-result (gensym "result")]
      `(let [~local-x (result ~x)
             ~local-result ~(if (seq? form)
                              `(result (~(first form) (value ~local-x) ~@(next form)))
                              `(result (list form x)))]
         (add-error ~local-result ~local-x)
         )))
  ([x form & more] `(merge-error-> (merge-error-> ~x ~form) ~@more)))

(defmacro until-error->
  "Threads the results through the forms until the first error.
  Inserts the result value as the second item in the first form, making a list of
  it if it is not a list already. If there are more forms, inserts the first
  form as the second item in second form, etc."
  {:added "1.0"}
  ([x] `(result ~x))
  ([x form]
    (let [local-x (gensym "local")]
      `(let [~local-x (result ~x)]
         (if (not (has-error? ~local-x))
           ~(if (seq? form)
              `(result (~(first form) (value ~local-x) ~@(next form)))
              `(result (list form x)))
           ~local-x)
         )))
  ([x form & more] `(until-error-> (until-error-> ~x ~form) ~@more)))


