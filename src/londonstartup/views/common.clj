(ns londonstartup.views.common
  (:require [londonstartup.environment :as env]
            [londonstartup.services.session :as session])
  (:use [hiccup.page :only [include-css include-js html5]]
        [hiccup.def :only [defhtml]]
        [hiccup.element :only [link-to]]
        [hiccup.form]
        [noir.request]))

(def menu-definition (array-map "Home" "/" "Jobs" "#fakelink" "About" "#fakelink" "More" (array-map "Add New Startup" "/add/startups" "My Settings" "/settings")))

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
  (let [config (:search config)
        enabled (:enabled config true)
        query (:query config)]
    (if enabled
      (form-to {:class "navbar-search"} [:get "/startups"]
        (text-field {:placeholder "Search" :class "span4"} :query query)))))

(defn navbar-login [config]
  (let [config (:login config)
        enabled (:enabled config true)]
    (if enabled
      (if-let [user (session/get :user)]
        (form-to {:class "navbar-form pull-right"} [:get "/logout"]
          [:button.btn {:type "submit"} (str "Sign out " (:username user))])
        (form-to {:class "navbar-form pull-right"} [:get "/login"]
          (hidden-field :auto "true")
          [:button.btn {:type "submit"} [:span [:img {:src "/img/bird_blue_16.png"}] " Sign in with Twitter"]])
        ))))

;defn navbar-login [& username]
;  (if (session/user-logged?)
;    [:span.pull-right "Welcome " (session/username) [:a.btn {:href "/logout"} "Sign out"]]
;    [:a.btn.pull-right {:href "/login"} [:img {:src "/img/bird_blue_16.png"}] " Sign in with Twitter"]))


(defn navbar [config]
  [:div.navbar.navbar-inverse.navbar-fixed-top [:div.navbar-inner [:div.container-fluid (brand "SD/London")
                                                                   (nav-collapse-button "#nav-collapse")
                                                                   [:div#nav-collapse.nav-collapse.collapse (menu menu-definition "nav")
                                                                    (navbar-search config)
                                                                    (navbar-login config)
                                                                    ]]]])

(defn flashbar []
  (if-let [message (session/flash)]
    [:div.row.flash [:div.span12 [:h1 message]]]))

;<div class="alert-messages  hidden" id="message-drawer">
;        <div class="message ">
;      <div class="message-inside">
;        <span class="message-text">Wrong Username/Email and password combination.</span><a class="dismiss" href="#">×</a>
;      </div>
;    </div></div>

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

(defn debug []
  (if env/debug? [:div.container-fluid.debug
                  [:div.row-fluid [:div {:class "span12"} (str @noir.session/*noir-session*)]]
                  [:div.row-fluid [:div {:class "span12"} (str noir.request/*request*)]]
                  [:div.row-fluid [:div {:class "span12"} (str (System/getenv))]]
                  [:div.row-fluid [:div {:class "span12"} (str (System/getProperties))]]]))

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

       (include-css "/bootstrap/css/bootstrap.css")
       (include-css "/bootstrap/css/bootstrap-responsive.css")
       (include-css "/bootstrap/css/docs.css")

       (include-css "/css/londonstartup.css")]

      [:body (navbar navbar-config) (flashbar) (debug) [:div#wrapper content] javascript])))
