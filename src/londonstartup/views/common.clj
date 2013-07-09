(ns londonstartup.views.common
  (:require [londonstartup.environment :as env]
            [noir.session :as session])
  (:use [hiccup.page :only [include-css include-js html5]]
        [hiccup.def :only [defhtml]]
        [hiccup.element :only [link-to]]
        [hiccup.form]))

(def menu-definition (array-map "Home" "/" "Jobs" "#fakelink" "About" "#fakelink" "More" (array-map "Add New Startup" "/add/startups" "My Settings" "#fakelink")))

(declare menu)

(defn dropdown-menu [label menu]
  [:li.dropdown [:a.dropdown-toggle {:href "#" :data-toggle "dropdown"} label [:b.caret ]]
   menu])

(defn menu [definition class]
  (reduce
    (fn [acc entry]
      (let [label (first entry)
            target (second entry)]
        (if (coll? target)
          (conj acc (dropdown-menu label (menu target "dropdown-menu")))
          (conj acc [:li (link-to target label)]))))
    [:ul {:class class}] definition))

(defn nav-collapse-button [target]
  [:a.btn.btn-navbar.collapsed {:data-toggle "collapse" :data-target target}
   [:span.icon-bar ]
   [:span.icon-bar ]
   [:span.icon-bar ]])

(defn brand [name]
  [:a.brand.pull-left {:href "#"} name])

(defn navbar-search [config]
  (let [enabled (:enabled config true)
        query (:query config)]
    (if enabled
      (form-to {:class "navbar-search"} [:get "/startups"]
        (text-field {:placeholder "Search" :class "span4"} :query query)))))

(defn navbar-login [& username]
  [:form.navbar-form.pull-right [:input#twitterId {:type "text" :placeholder "@TwitterId"}]
   [:button.btn {:type "submit"} "Sign in"]])

(defn navbar [config]
  (let [search-config (:search config)]
    [:div.navbar.navbar-inverse.navbar-fixed-top [:div.navbar-inner [:div.container-fluid (brand "SD/London")
                                                                     (nav-collapse-button "#nav-collapse")
                                                                     [:div#nav-collapse.nav-collapse.collapse (menu menu-definition "nav")
                                                                      (navbar-search search-config)
                                                                      (navbar-login)
                                                                      ]]]]))

(defn searchbar [& query]
  [:section.container-fluid [:div.row-fluid [:div.span8.offset2 (form-to {:class "form-inline"} [:get "/startups"]
                                                                  (text-field {:class "span8"} :query (first query))
                                                                  (submit-button {:class "btn"} "Search")
                                                                  (link-to {:class "btn"} "/startups" "List All")
                                                                  )]]])

(def javascript
  (include-js "http://code.jquery.com/jquery-1.10.1.min.js"
    "/bootstrap/js/bootstrap.min.js"
    "/js/application.js"))

(defhtml layout [& content]
  (let [split-navbar-config (split-with :navbar content)
        content (second split-navbar-config)
        navbar-config (:navbar (first (first split-navbar-config)))]
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

      [:body (navbar navbar-config) (if env/debug? [:div.row.debug [:div {:class "span12.hide"} (str {:user (session/get :user)})]])[:div#wrapper content] javascript])))
