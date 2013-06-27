(ns londonstartup.views.home
  (:require
            [clojure.string :as string]
            [londonstartup.views.common :as common]
            [londonstartup.views.bootstrap :as bs])
  (:use hiccup.core
        hiccup.element
        hiccup.page
        hiccup.form
        hiccup.def))


(defn home-page []
  (common/layout
    {:navbar {:search {:enabled false}}}
    [:header.jumbotron.subhead [:div.container [:h1 "London Startup Directory"]
                                [:p.lead "The open startup reference in London."]
                                [:a.btn.btn-large.btn-danger {:href "/add/startups"} "Add Your Startup Now, It is Free!"]]]
    (common/searchbar)))