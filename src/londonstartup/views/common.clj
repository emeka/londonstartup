(ns londonstartup.views.common
  (:use [hiccup.page :only [include-css]]
        [hiccup.def :only [defhtml]]))

(defhtml layout [& content]
              [:head
               [:title "londonstartup"]
               (include-css "/css/reset.css")]
              [:body
               [:div#wrapper
                content]])
