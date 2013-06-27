(ns londonstartup.controllers.home
  (:require [ring.util.response :as resp]
            [londonstartup.views.home :as views]
            [londonstartup.common.result :as result]))

(defn home []
  (views/home-page))