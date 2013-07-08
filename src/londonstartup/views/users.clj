(ns londonstartup.views.users
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
(defn get-field [user-result key & [default]]
  (let [field-value (key (result/value user-result))]
    (if (and (not (nil? field-value)) (not= "" field-value))
      field-value
      default)))

(defn get-errors [user-result key]
  (key (result/errors user-result)))

;; Page Elements

(defn id [{:keys [_id]} & suffix]
  (let [prefix (str "user-" _id)
        suffix (first suffix)]
    (if suffix
      (str prefix "-" suffix)
      prefix)))

(defn idref [user & suffix]
  (str "#" (id user (first suffix))))

(defn get-link [{:keys [username]} & content]
  (link-to (str "/users/" username) content))

(defn update-link [{:keys [username]} & content]
  (link-to (str "/update/users/" username) content))

(defn add-link [content]
  (link-to "/add/users" content))

;;Forms
(defn error-text [errors]
  (reduce #(conj %1 %2 [:br ]) [:span.help-inline ] errors))

(defn user-fields [{:keys [website username twitterAccount githubAccount linkdedInAccount phone _id]} errors]
  (list
    (bs/row
      (bs/span6 (bs/control-group :username "username" (text-field {:placeholder ""} :username username) (validate/on-error errors :username error-text))
        (bs/control-group :website "Website" (text-field {:placeholder "Website"} :website website) (validate/on-error errors :website error-text))
        (bs/control-group :twitterAccount "Twitter Account" (text-field {:placeholder "@Example"} :twitterAccount twitterAccount))
        (bs/control-group :githubAccount "Github" (text-field {:placeholder "@Example"} :githubAccount githubAccount))
        (bs/control-group :linkdedInAccount "LinkedIn" (text-field {:placeholder "Example"} :linkdedInAccount linkdedInAccount))
        (bs/control-group :phone "Phone" (text-field {:placeholder "ex: +44 20 7123 1234"} :phone phone)))
      (hidden-field :_id _id))))

(defn user-remove-form [{:keys [username]}]
  (when username
    (form-to [:delete (str "/users/" username)] ;The url should be calculated from the route
      (submit-button {:class "btn btn-danger"} "Delete"))))

(defn user-form [action method url user errors]
  (form-to {:class "form-horizontal"} [method url]
    (user-fields user errors)
    (bs/row
      (bs/span4 [:a.btn {:href url} "Cancel"]
        (submit-button {:class "btn btn-primary"} action)))))

;;Header
(defn badge [icon value]
  [:small (bs/icon icon) " " value " "])

(defn user-edit-button [user]
  [:small (update-link user [:i.icon-edit ])])

(defn user-username [{:keys [username] :as user}]
  [:div.user-header.span6 [:h2 (get-link user username) [:small (update-link user [:i.icon-edit ])]]])

(defn user-badges [user]
  [:div.user-badges.span4.offset2 [:h2 (badge "icon-user" 13) (badge "icon-bullhorn" 2450) (badge "icon-heart" 200)]])

(defn user-header [user]
  [:div.row (user-username user) (user-badges user)])


;;Details
(defn phone [{:keys [phone]}]
  [:span [:strong "Phone"] [:br ] [:span phone] [:br ] [:br ]])

(defn website [{:keys [website]}]
  [:span [:strong "Website"] [:br ]
   (let [domain-name (string/replace website "http://" "")]
     (link-to (str "http://" domain-name) domain-name)) [:br ] [:br ]])

(defn twitterAccount [{:keys [twitterAccount]}]
  [:span [:strong "Twitter"] [:br ] [:span twitterAccount] [:br ] [:br ]])
(defn githubAccount [{:keys [githubAccount]}]
  [:span [:strong "Github"] [:br ] [:span githubAccount] [:br ] [:br ]])
(defn linkedInAccount [{:keys [linkedInAccount]}]
  [:span [:strong "LinkedIn"] [:br ] [:span linkedInAccount] [:br ] [:br ]])

;;Details

;;One user
(defn user-summary [{:keys [_id website username] :as user}]
  (when user
    [:section.user.container-fluid {:id (id user)}
     (user-header user)]))

(defn user-dashboard [user]
  (when user
    [:section.user.container-fluid {:id (id user)}
     (user-header user)
     [:div.row [:div.span3 (website user) (twitterAccount user) (githubAccount user) (linkedInAccount user) (phone user)]
      ]]
    ))

;;Several users
(defn user-list [users]
  (map #(user-summary %) users))

;; Pages
(defn user-page [user-result]
  (common/layout
    (user-dashboard (result/value user-result)))) ;The url should be calculated from the route

(defn users-page [users-result query]
  (common/layout
    {:navbar {:search {:query query}}}
    (user-list (result/value users-result))))

;;Form Pages

(defn add-user-page [user-result]
  (let [user (result/value user-result)
        errors (result/errors user-result)]
    (common/layout
      {:navbar {:search {:enabled false}}}
      [:header.jumbotron.subhead [:div.container [:h1 "Add New user"]]]
      [:div.container-fluid [:div.row-fluid [:div.span12 (add-link   )]]]))) ;The url should be calculated from the route

(defn update-user-page [user-result & [default-website]]
  (let [website (get-field user-result :website default-website)
        user (result/value user-result)
        errors (result/errors user-result)]
    (common/layout
      [:header.container-fluid (user-header user)]
      [:div.container-fluid [:div.row-fluid [:div.span12 (user-form "Update" :put (str "/users/" website) user errors) ;The url should be calculated from the route
                                             (user-remove-form user)]]])))