(ns londonstartup.views.auth
  (:require [clojure.string :as string]
            [londonstartup.views.common :as common]
            [londonstartup.views.bootstrap :as bs]
            [londonstartup.common.result :as result]
            [londonstartup.common.validation :as validate])
  (:use hiccup.core
        hiccup.element
        hiccup.page
        hiccup.form
        hiccup.def))


;Access denied

