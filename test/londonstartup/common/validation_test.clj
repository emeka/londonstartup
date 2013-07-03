(ns londonstartup.common.validation-test
  (:require [londonstartup.common.validation :as validate]
            [londonstartup.common.result :as result])
  (:use clojure.test))

(deftest has-value?
  (let [test-result (validate/has-value? {:foo "Value"} :foo "Msg")]
    (is (not (result/has-error? test-result)))
    (is (= {:foo "Value"} (result/value test-result))))
  (let [test-result (validate/has-value? {:foo ""} :foo "Msg")]
    (is (result/has-error? test-result))
    (is (= {:foo ""} (result/value test-result)))
    (is (= ["Msg"] (:foo (result/errors test-result)))))
  (let [test-result (validate/has-value? {:foo "Value"} :bar "Msg")]
    (is (result/has-error? test-result))
    (is (= {:foo "Value"} (result/value test-result)))
    (is (= ["Msg"] (:bar (result/errors test-result)))))
  (is (result/has-error? (validate/has-value? {:foo "Value"} :bar "Msg")))
  )