(ns londonstartup.views.startups
  (:require [clojure.string :as string]
            [londonstartup.views.common :as common]
            [londonstartup.views.bootstrap :as bs]
            [londonstartup.common.result :as result]
            [londonstartup.common.validation :as validate])
  (:use hiccup.core
        hiccup.element
        hiccup.page
        hiccup.form
        hiccup.def))


;;Helpers
(defn get-field [startup-result key & [default]]
  (let [field-value (key (result/value startup-result))]
    (if (and (not (nil? field-value)) (not= "" field-value))
      field-value
      default)))

(defn get-errors [startup-result key]
  (key (result/errors startup-result)))

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
  (reduce #(conj %1 %2 [:br ]) [:span.help-inline ] errors))

(defn startup-fields [{:keys [website name twitterAccount phone addressLine1 addressLine2 addressLine3 city county country postCode _id]} errors]
  (list
    (bs/row
      (bs/span6 (bs/control-group :name "Name" (text-field {:placeholder "Name"} :name name) (validate/on-error errors :name error-text))
        (bs/control-group :website "Website" (text-field {:placeholder "Website"} :website website) (validate/on-error errors :website error-text))
        (bs/control-group :twitterAccount "Twitter Account" (text-field {:placeholder "@Example"} :twitterAccount twitterAccount))
        (bs/control-group :phone "Phone" (text-field {:placeholder "ex: +44 20 7123 1234"} :phone phone)))

      (bs/span6
        (bs/control-group :address "Address"
          (text-field {:placeholder "Address Line 1"} :addressLine1 addressLine1) [:br ]
          (text-field {:placeholder "Address Line 2"} :addressLine2 addressLine2) [:br ]
          (text-field {:placeholder "Address Line 3"} :addressLine3 addressLine3))

        (bs/control-group :city "City" (text-field {:placeholder "ex: London"} :city city))
        (bs/control-group :county "County" (text-field {:placeholder "ex: Greater London"} :county county))
        (bs/control-group :postCode "Post Code" (text-field {:placeholder "ex: SW1W 9XX"} :postCode postCode))
        (bs/control-group :country "Country" (text-field {:placeholder "ex: UK"} :country country)))

      (hidden-field :_id _id))))

(defn startup-remove-form [{:keys [website]}]
  (when website
    (form-to [:delete (str "/startups/" website)] ;The url should be calculated from the route
      (submit-button {:class "btn btn-danger"} "Delete"))))

(defn startup-form [action method url startup errors]
  (form-to {:class "form-horizontal"} [method url]
    (startup-fields startup errors)
    (bs/row
      (bs/span4 [:a.btn {:href url} "Cancel"]
        (submit-button {:class "btn btn-primary"} action)))))

;;Header
(defn badge [icon value]
  [:small (bs/icon icon) " " value " "])

(defn startup-edit-button [startup]
  [:small (update-link startup [:i.icon-edit ])])

(defn startup-name [{:keys [name] :as startup}]
  [:div.startup-header.span6 [:h2 (get-link startup name) [:small (update-link startup [:i.icon-edit ])]]])



(defn startup-badges [startup]
  [:div.startup-badges.span4.offset2 [:h2 (badge "icon-user" 13) (badge "icon-bullhorn" 2450) (badge "icon-heart" 200)]])

(defn startup-header [startup]
  [:div.row (startup-name startup) (startup-badges startup)])


;;Details
(defn address [{:keys [name addressLine1 addressLine2 addressLine3 city county country postCode]}]
  [:address [:strong "Address"] [:br ]
   [:span addressLine1] [:br ]
   [:span addressLine2] [:br ]
   [:span addressLine3] [:br ]
   [:span city] [:br ]
   [:span county] [:br ]
   [:span country] [:br ]
   [:span postCode] [:br ] [:br ]
   ]
  )

(defn phone [{:keys [phone]}]
  [:span [:strong "Phone"] [:br ] [:span phone] [:br ] [:br ]])

(defn website [{:keys [website]}]
  [:span [:strong "Website"] [:br ]
   (let [domain-name (string/replace website "http://" "")]
     (link-to (str "http://" domain-name) domain-name)) [:br ] [:br ]])

(defn twitterAccount [{:keys [twitterAccount]}]
  [:span [:strong "Twitter"] [:br ] [:span twitterAccount] [:br ] [:br ]])

;;Details
(defn location [{:keys [name website] :as startup}]
  (str "<div>
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
     [:div.row [:div.span3 (address startup) (phone startup) (website startup) (twitterAccount startup)]
      [:div.span3 (location startup)]
      ]]
    ))

;;Several startups
(defn startup-list [startups]
  (map #(startup-summary %) startups))

;; Pages
(defn startup-page [startup-result]
  (common/layout
    (startup-dashboard (result/value startup-result)))) ;The url should be calculated from the route

(defn startups-page [startups-result query]
  (common/layout
    {:navbar {:search {:query query}}}
    (startup-list (result/value startups-result))))

;;Form Pages

(defn add-form [startup errors]
  [:div.row-fluid [:div.span12 [:div.module [:h1 "Please tell us about your startup:"]
                                (startup-form "Add" :post "/startups" startup errors)]]])

(defn update-form [startup errors]
  [:div.row-fluid [:div.span12 [:div.module [:h1 "Update your startup profile:"]
                                (startup-form "Update" :put (str "/startups/" website) startup errors)
                                (startup-remove-form startup)]]])

(defn add-startup-page [startup-result]
  (let [startup (result/value startup-result)
        errors (result/errors startup-result)]
    (common/layout
      {:navbar {:search {:enabled false}}}
      [:header.jumbotron.subhead [:div.container [:h1 "Add New Startup"]]]
      [:div.container-fluid (add-form startup errors)]))) ;The url should be calculated from the route

(defn update-startup-page [startup-result & [default-website]]
  (let [website (get-field startup-result :website default-website)
        startup (result/value startup-result)
        errors (result/errors startup-result)]
    (common/layout
      [:header.jumbotron.subhead [:div.container [:h1 (str (:name startup))]]]
      [:div.container-fluid (update-form startup errors)])))