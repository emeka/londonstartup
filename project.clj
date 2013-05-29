(defproject londonstartup "0.1.0-SNAPSHOT"
            :description "London Startup Directory"
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [noir "1.3.0-beta3"]]
            :license {:name "Simplified BSD License"
                      :url "http://en.wikipedia.org/wiki/BSD_licenses"}
            :main londonstartup.server
            :min-lein-version "2.0.0"
            :plugins [[environ/environ.lein "0.2.1"]]
            :hooks [environ.leiningen.hooks]
            :profiles {:production {:env {:production true}}})