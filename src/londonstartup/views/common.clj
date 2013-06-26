(ns londonstartup.views.common
  (:use [hiccup.page :only [include-css include-js html5]]
        [hiccup.def :only [defhtml]]
        [hiccup.element :only [link-to]]))

(def menu-definition {"Home" "/" "Jobs" "#fakelink" "About" "#fakelink" "More" {"Add New Startup" "/add/startups" "My Settings" "#fakelink"}})

(declare menu)

(defn dropdown-menu [label menu]
  [:li.dropdown [:a.dropdown-toggle {:href "#" :data-toggle "dropdown"} label [:b.caret]]
   menu])

(defn menu [definition class]
  (reduce
    (fn [acc entry]
      (let [label (first entry)
            target (second entry)]
        (if (coll? target)
          (conj acc (dropdown-menu label (menu target "dropdown-menu")))
          (conj acc [:li (link-to target label)]))))
    [:ul {:class class}] (reverse definition)))

(defn nav-collapse-button [target]
  [:a.btn.btn-navbar.collapsed {:data-toggle "collapse" :data-target target}
   [:span.icon-bar ]
   [:span.icon-bar ]
   [:span.icon-bar ]])

(defn brand [name]
  [:a.brand.pull-left {:href "#"} name])

(def navbar-search
  [:form.navbar-search [:input.span4 {:type "text" :placeholder "Search"}]])

(def navbar-login
  [:form.navbar-form.pull-right [:input#twitterId {:type "text" :placeholder "@TwitterId"}]
   [:button.btn {:type "submit"} "Sign in"]])

(def navbar
  [:div.navbar.navbar-inverse.navbar-fixed-top [:div.navbar-inner [:div.container-fluid (brand "SD/London")
                                                                   (nav-collapse-button "#nav-collapse")
                                                                   [:div#nav-collapse.nav-collapse.collapse (menu menu-definition "nav")
                                                                    navbar-search
                                                                    navbar-login
                                                                    ]]]])

(def javascript
  (include-js "http://code.jquery.com/jquery-1.10.1.min.js"
    "/bootstrap/js/bootstrap.min.js"
    "/js/application.js"))


(defhtml layout [& content]
  (html5
    [:head [:meta {:charset "utf-8"}]
     [:meta {:viewport "width=device-width, initial-scale=1.0"}]
     [:meta {:description "Londond startup directory"}]
     [:meta {:author "Emeka Mosanya"}]
     [:title "London Startup Directory"]

     (include-css "http://twitter.github.io/bootstrap/assets/css/bootstrap.css")
     (include-css "http://twitter.github.io/bootstrap/assets/css/bootstrap-responsive.css")
     (include-css "http://twitter.github.io/bootstrap/assets/css/docs.css")

     (include-css "/css/londonstartup.css")

     [:link {:rel "apple-touch-icon-precomposed" :sizes "144x144"
             :href "http://twitter.github.io/bootstrap/assets/ico/apple-touch-icon-144-precomposed.png"}]
     [:link {:rel "apple-touch-icon-precomposed" :sizes "114x114"
             :href "http://twitter.github.io/bootstrap/assets/ico/apple-touch-icon-114-precomposed.png"}]
     [:link {:rel "apple-touch-icon-precomposed" :sizes "72x72"
             :href "http://twitter.github.io/bootstrap/assets/ico/apple-touch-icon-72-precomposed.png"}]
     [:link {:rel "apple-touch-icon-precomposed"
             :href "http://twitter.github.io/bootstrap/assets/ico/apple-touch-icon-57-precomposed.png"}]
     [:link {:rel "shortcut icon" :href "http://twitter.github.io/bootstrap/assets/ico/favicon.png"}]]

    [:body navbar [:div#wrapper content] javascript]))
