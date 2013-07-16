(defproject londonstartup "0.1.0-SNAPSHOT"
  :description "London Startup Directory"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [lib-noir "0.6.6"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.3"]
                 [com.novemberain/monger "1.5.0"]
                 [twitter-api "0.7.4"]]
  :license {:name "Simplified BSD License"
            :url "http://en.wikipedia.org/wiki/BSD_licenses"}
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"]
            [lein-ring "0.8.5"]]
  :ring {:init londonstartup.handler/init
         :handler londonstartup.handler/app}
  :hooks [environ.leiningen.hooks]
  :profiles {:production {:env {:production true}}
             :dev {:dependencies [[midje "1.5.1"]
                                  [ring-mock "0.1.5"]
                                  [org.slf4j/slf4j-nop "1.6.4"]]
                   :plugins [[lein-embongo "0.2.1"]
                             [lein-midje "3.0.1"]]}})
