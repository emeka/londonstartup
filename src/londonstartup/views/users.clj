(ns londonstartup.views.users
  (:require [clojure.string :as string]
            [londonstartup.views.common :as common]
            [londonstartup.views.bootstrap :as bs]
            [londonstartup.common.result :as result]
            [londonstartup.common.validation :as validate]
            [londonstartup.environment :as env])
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

(defn user-fields [{:keys [username website githubAccount linkdedInAccount googlePlusAccount _id]} errors]
  (list
    (bs/row
      (bs/span6
        ;(bs/control-group :username "Username" (text-field {:class "uneditable-input" :placeholder ""} :username username) (validate/on-error errors :username error-text))
        ;(bs/control-group :website "Website" (text-field {:class "uneditable-input"  :placeholder "www.example.com"} :website website) (validate/on-error errors :website error-text))
        (bs/control-group :githubAccount "Github" (text-field {:placeholder "Account name"} :githubAccount githubAccount))
        (bs/control-group :linkdedInAccount "LinkedIn" (text-field {:placeholder "Account name"} :linkdedInAccount linkdedInAccount))
        (bs/control-group :googlePlusAccount "Google +" (text-field {:placeholder "Account name"} :googlePlusAccount googlePlusAccount)))
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
(defn field [name value]
  (when value
    [:span [:strong name] [:br ] [:span value] [:br ] [:br ]]))

(defn phone [{:keys [phone]}]
  (field "Phone" phone))

(defn website [{:keys [website]}]
  (when website
    (let [domain-name (string/replace website "http://" "")
          link (link-to (str "http://" domain-name) domain-name)]
      (field "Website" link))))

(defn twitterAccount [{:keys [twitterAccount]}]
  (field "Twitter" twitterAccount))

(defn githubAccount [{:keys [githubAccount]}]
  (field "Github" githubAccount))

(defn linkedInAccount [{:keys [linkedInAccount]}]
  (field "LinkedIn" linkedInAccount))

(defn googlePlusAccount [{:keys [googlePlusAccount]}]
  (field "Google Plus" googlePlusAccount))

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
     [:div.row [:div.span3 (website user) (twitterAccount user) (githubAccount user) (linkedInAccount user) (googlePlusAccount user) (phone user)]
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

(defn signup-form [{:keys [username] :as user} errors]
  [:div.row-fluid [:div.span6.offset3 [:div.module [:h1 "Please tell us more about you:"]
                                                   (user-form "Update" :put (str "/users/" username) user errors)]]])

(defn profile-form [{:keys [username] :as user} errors]
  [:div.row-fluid [:div.span6.offset3 [:div.module [:h1 "Update your profile:"]
                                                   (user-form "Update" :put (str "/users/" username) user errors)]]])


(defn debug [value]
  (if env/debug? [:div.row.debug [:div {:class "span12.hide"} (str value)]]))

(defn signup-page [user-result uri]
  (let [user (result/value user-result)
        errors (result/errors user-result)]
    (common/layout
      {:navbar {:search {:enabled false} :login {:enabled false}}}
      [:header.jumbotron.subhead [:div.container [:h1 (str "Welcome " (:username user))]]]
      [:div.container-fluid (signup-form user errors)])))

(defn update-user-page [user-result & [default-username]]
  (let [username (get-field user-result :username default-username)
        user (result/value user-result)
        errors (result/errors user-result)]
    (common/layout
      [:header.jumbotron.subhead [:div.container [:h1 (str (:username user))]]]
      (debug user)
      [:div.container-fluid (profile-form user errors)])))