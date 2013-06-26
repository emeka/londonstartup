(ns londonstartup.views.startups
  (:require [noir.validation :as validate]
            [clojure.string :as string]
            [londonstartup.views.common :as common]
            [londonstartup.views.bootstrap :as bs])
  (:use hiccup.core
        hiccup.element
        hiccup.page
        hiccup.form
        hiccup.def))

;;Validation
;(defn valid? [{:keys [website name]}]
;  (validate/rule (validate/has-value? website)
;    [:website "A startup must have a website"])
;  (validate/rule (validate/has-value? name)
;    [:name "A startup must have a name"])
;  ;(not (validate/errors? :website :name)) ;;TODO: valid? should be at view level.
;  )

;; Page Elements

(defn id [{:keys [_id]} & suffix]
  (let [prefix (str "startup-" _id)
        suffix (first suffix)]
    (if suffix
      (str prefix "-" suffix)
      prefix)))

(defn idref [startup & suffix]
  (str "#" (id startup (first suffix))))

(defn get-link [{:keys [website]} & content]
  (link-to (str "/startups/" website) content))

(defn update-link [{:keys [website]} & content]
  (link-to (str "/update/startups/" website) content))

;;Forms
(defn error-text [errors]
  [:span (string/join "" errors)])

(defn startup-fields [{:keys [website name _id]}]
  ;(validate/on-error :name error-text)
  (list
    [:div.control-group (label {:class "control-label"} :name "Name")
     [:div.controls (text-field {:placeholder "Name"} :name name)]]

    [:div.control-group ;(validate/on-error :website error-text)
     (label {:class "control-label"} :website "Website")
     [:div.controls (text-field {:placeholder "Website"} :website website)]]
    (hidden-field :_id _id)))

(defn startup-remove-form [{:keys [website]}]
  (when website
    (form-to [:delete (str "/startups/" website)] ;The url should be calculated from the route
      (submit-button {:class "btn btn-danger"} "Delete"))))

(defn startup-form [action method url startup]
  (form-to [method url]
    (startup-fields startup)
    [:a.btn {:href url} "Cancel"]
    (submit-button {:class "btn btn-primary"} action)
    ))

;;Header
(defn badge [icon value]
  [:small (bs/icon icon) " " value " "])

(defn startup-edit-button [startup]
  [:small (update-link startup [:i.icon-edit ])])

(defn startup-name [{:keys [name] :as startup}]
  [:div.startup-header.span6 [:h2 (get-link startup name)]])



(defn startup-badges [startup]
  [:div.startup-badges.span4.offset2 [:h2 (badge "icon-user" 13) (badge "icon-bullhorn" 2450) (badge "icon-heart" 200) [:small (update-link startup [:i.icon-edit])]]])

(defn startup-header [startup]
  [:div.row (startup-name startup) (startup-badges startup)])


;;Details
(defn address [{:keys [name website] :as startup}]
  [:address.span3 [:strong name] [:br ]
   [:span "Belgrave House"] [:br ]
   [:span "76 Buckingham Palace Rd"] [:br ]
   [:span "London"] [:br ]
   [:span "Greater London"] [:br ]
   [:span "SW1W 9TQ"] [:br ] [:br ]

   [:strong "Phone"] [:br ]
   [:span "020 7031 3000"] [:br ] [:br ]
   [:strong "Website"] [:br ]
   (let [domain-name (string/replace website "http://" "")]
     (link-to (str "http://" domain-name) domain-name))
   ]
  )

;;Details
(defn location [{:keys [name website] :as startup}]
  (str "<div class=\"span3\">
                      <iframe width=\"300\" height=\"300\" frameborder=\"0\" scrolling=\"no\" marginheight=\"0\" marginwidth=\"0\"
                              src=\"https://maps.google.co.uk/maps?f=q&amp;source=s_q&amp;hl=en&amp;geocode=&amp;q=Google+London,+Buckingham+Palace+Road,+London&amp;aq=0&amp;oq=google+lon&amp;sll=51.48931,-0.08819&amp;sspn=1.061176,1.253815&amp;ie=UTF8&amp;hq=Google+London,+Buckingham+Palace+Road,+London&amp;t=m&amp;ll=51.494904,-0.146427&amp;spn=0.008016,0.012875&amp;z=15&amp;iwloc=near&amp;output=embed\"></iframe>
                      <br/>
                      <small><a
                              href=\"https://maps.google.co.uk/maps?f=q&amp;source=embed&amp;hl=en&amp;geocode=&amp;q=Google+London,+Buckingham+Palace+Road,+London&amp;aq=0&amp;oq=google+lon&amp;sll=51.48931,-0.08819&amp;sspn=1.061176,1.253815&amp;ie=UTF8&amp;hq=Google+London,+Buckingham+Palace+Road,+London&amp;t=m&amp;ll=51.494904,-0.146427&amp;spn=0.008016,0.012875&amp;z=15&amp;iwloc=A\"
                              >View Larger Map</a></small>
                  </div>")
  )

;;One startup
(defn startup-summary [{:keys [_id website name] :as startup}]
  (when startup
    [:section.startup.container-fluid {:id (id startup)}
     (startup-header startup)]))

(defn startup-dashboard [startup]
  (when startup
    [:section.startup.container-fluid {:id (id startup)}
     (startup-header startup)
     [:div.row (address startup)
      (location startup)
      ]]
    ))

;;Several startups
(defn startup-list [startups]
  (map #(startup-summary %) startups))

;; Pages
(defn startup-page [{:keys [website] :as startup}]
  (common/layout
    (startup-dashboard startup))) ;The url should be calculated from the route

(defn startups-page [new-startup startups]
  (common/layout
    [:header.jumbotron.subhead [:div.container [:h1 "London Startup Directory"]
                                [:p.lead "The open startup reference in London."]
                                [:a.btn.btn-large.btn-danger {:href "/add/startups"} "Add Your Startup Now!"]]]
    (startup-list startups)))

;;Form Pages

(defn add-startup-page []
  (common/layout
    [:header.jumbotron.subhead [:div.container [:h1 "Add New Startup"]]]
    [:div.container-fluid [:div.row-fluid [:div.span12 (startup-form "Add" :post "/startups" {})]]])) ;The url should be calculated from the route

(defn update-startup-page [{:keys [website] :as startup}]
  (common/layout
    [:header.container-fluid (startup-header startup)]
    [:div.container-fluid [:div.row-fluid [:div.span12
    (startup-form "Update" :put (str "/startups/" website) startup) ;The url should be calculated from the route
    (startup-remove-form startup)]]]))