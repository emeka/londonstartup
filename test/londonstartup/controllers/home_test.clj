(ns londonstartup.controllers.home-test
  (:require [londonstartup.controllers.home :as home])
  (:use midje.sweet))

(fact "Controller home just calls the home view once"
  (home/home) => :whatever
  (provided (home/home) => :whatever :times 1))