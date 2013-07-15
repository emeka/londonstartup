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


(defn login-form [redirect-to]
  [:div.login [:h1 "Startup Directory uses Twitter for Authentication"]
   [:a.btn {:href (str "/login?auto=true&redirect_to=" redirect-to)} [:span [:img {:src "/img/bird_blue_16.png"}] " Sign in with Twitter"]]])

(defn login-page
  ([] (login-page "/"))
  ([redirect-to] (common/layout
                   {:navbar {:search {:enabled false} :login {:enabled false}}}
                   [:div.container-fluid [:div.row-fluid [:div.span6.offset3 (login-form redirect-to)]]])))



;Sign up
;
;