(ns londonstartup.views.bootstrap-test
  (:require [londonstartup.views.bootstrap :as bs])
  (:use clojure.test))

(deftest icon
  (is (= [:i {:class "icon-name"}] (bs/icon "icon-name"))))

(deftest tabs
  (is (= [:div.tabbable.collapse {:id "tab-id"} [:ul.nav.nav-tabs [:li [:a {:href "#startup1-location", :data-toggle "tab"} "Location"]] [:li [:a {:href "#startup2-location", :data-toggle "tab"} "Location2"]]] [:div.tab-content [:div.tab-pane {:id "startup1-location"} "content"] [:div.tab-pane {:id "startup2-location"} "content"]]]
        (bs/tabs "tab-id" {:id "startup1-location" :tab "Location" :content "content"} {:id "startup2-location" :tab "Location2" :content "content"}))))

