(ns londonstartup.common.result-test
  (:require [londonstartup.common.result :as result])
  (:use clojure.test))

(deftest add-error
  (let [init-result (result/result nil)
        result1 (result/add-error init-result :website "Error1")
        result2 (result/add-error result1 :website "Error2")
        result3 (result/add-error result2 :name "Name Error")]
    (is (= {:value nil :errors {:website ["Error1"]}} result1))
    (is (= {:value nil :errors {:website ["Error1" "Error2"]}} result2))
    (is (= {:value nil :errors {:website ["Error1" "Error2"] :name ["Name Error"]}} result3))))

(deftest result
  (is (= {:value nil} (result/result nil)))
  (is (= {:value 3} (result/result 3))))

(deftest error
  (is (= {:value "Value" :errors {:website ["Error"]}} (result/error "Value" :website "Error"))))

(deftest has-error?
  (is (not (result/has-error? (result/result nil))))
  (is (result/has-error? (result/add-error (result/result nil) :website "Error"))))

(deftest error-free?
  (is (result/error-free? (result/result nil)))
  (is (not (result/error-free? (result/add-error (result/result nil) :website "Error")))))

(deftest errors
  (is (nil? (result/errors (result/result nil))))
  (is (= {:website ["Error"]} (result/errors (result/add-error (result/result nil) :website "Error")))))

(deftest value
  (is (nil? (result/value (result/result nil))))
  (is (= 3 (result/value (result/result 3)))))

(deftest result?
  (is (result/result? (result/result "Value")))
  (is (not (result/result? "Value"))))

(deftest until-error->
  (is (result/result? (result/until-error-> "Value")))
  (is (= "ValueFoo" (result/value (result/until-error-> "Value" (str "Foo")))))
  (let [x (result/until-error-> {:value "Value"} (str "Foo"))]
    (is (not (result/has-error? x)))
    (is (= "ValueFoo" (result/value x))))
  (let [x (result/until-error-> {:value "Value" :errors {:name ["Error"]}} (str "Foo"))]
    (is (result/has-error? x))
    (is (= "Value" (result/value x))))
  (let [x (result/until-error-> {:value "Value"} (str "Foo") (str "Bar"))]
    (is (not (result/has-error? x)))
    (is (= "ValueFooBar" (result/value x))))
  (let [x (result/until-error-> {:value "Value"} (#(conj (result/result %) {:errors "Error"})))]
    (is (result/has-error? x))
    (is (= "Value" (result/value x))))
  (let [x (result/until-error-> {:value "Value"} (#(conj (result/result %) {:errors "Error"})) (str "Bar"))]
    (is (result/has-error? x))
    (is (= "Value" (result/value x))))
  )

(deftest merge-error->
  (is (result/result? (result/merge-error-> "Value")))
  (is (= "ValueFoo" (result/value (result/merge-error-> "Value" (str "Foo")))))
  (let [x (result/merge-error-> {:value "Value"} (str "Foo"))]
    (is (not (result/has-error? x)))
    (is (= "ValueFoo" (result/value x))))
  (let [x (result/merge-error-> {:value "Value" :errors {:name ["Error"]}} (str "Foo"))]
    (is (result/has-error? x))
    (is (= "ValueFoo" (result/value x))))
  (let [x (result/merge-error-> {:value "Value"} (str "Foo") (str "Bar"))]
    (is (not (result/has-error? x)))
    (is (= "ValueFooBar" (result/value x))))
  (let [x (result/merge-error-> {:value "Value"} (#(conj (result/result %) {:errors "Error"})))]
    (is (result/has-error? x))
    (is (= "Value" (result/value x))))
  (let [x (result/merge-error-> {:value "Value"} (#(conj (result/result %) {:errors "Error"})) (str "Bar"))]
    (is (result/has-error? x))
    (is (= "ValueBar" (result/value x))))
  (let [x (result/merge-error-> {:value "Value"} (#(conj (result/result %) {:errors {:name ["Error1"]}})) (#(conj {:value (str % "Value2")} {:errors {:name ["Error2"]}})))]
    (is (result/has-error? x))
    (is (= ["Error1" "Error2"] (sort (:name (result/errors x)))))
    (is (= "ValueValue2" (result/value x))))
  )
