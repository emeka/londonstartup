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

(defn settings-url [{:keys [username]}]
  (str "/users/" username "/settings"))

(defn settings-link [user & content]
  (link-to (settings-url user) content))

(defn add-link [content]
  (link-to "/add/users" content))

;;Forms
(defn error-text [errors]
  (reduce #(conj %1 %2 [:br ]) [:span.help-inline ] errors))

(defn user-fields [{:keys [username website githubAccount linkedInAccount googlePlusAccount _id]} errors redirect_to]
  (list
    (bs/row
      (bs/span6
        ;(bs/control-group :username "Username" (text-field {:class "uneditable-input" :placeholder ""} :username username) (validate/on-error errors :username error-text))
        ;(bs/control-group :website "Website" (text-field {:class "uneditable-input"  :placeholder "www.example.com"} :website website) (validate/on-error errors :website error-text))
        (bs/control-group :githubAccount "Github" (text-field {:placeholder "Account name"} :githubAccount githubAccount))
        (bs/control-group :linkedInAccount "LinkedIn" (text-field {:placeholder "Account name"} :linkedInAccount linkedInAccount))
        (bs/control-group :googlePlusAccount "Google +" (text-field {:placeholder "Account name"} :googlePlusAccount googlePlusAccount)))
      (hidden-field :_id _id)
      (hidden-field :redirect_to redirect_to))))

(defn user-remove-form [{:keys [username]}]
  (when username
    (form-to [:delete (str "/users/" username)] ;The url should be calculated from the route
      (submit-button {:class "btn btn-danger"} "Delete"))))

(defn user-form [action method update-url redirect_to user errors]
  (form-to {:class "form-horizontal"} [method update-url]
    (user-fields user errors redirect_to)
    (bs/row
      (bs/span4 (submit-button {:class "btn btn-primary"} action)))))

;;Header
(defn badge [icon value]
  [:small (bs/icon icon) " " value " "])

(defn user-edit-button [user]
  [:small (settings-link user [:i.icon-edit ])])

(defn user-username [{:keys [username] :as user}]
  [:div.user-header.span6 [:h2 (get-link user username) [:small (settings-link user [:i.icon-edit ])]]])

(defn user-badges [user]
  [:div.user-badges.span4.offset2 [:h2 (badge "icon-user" 13) (badge "icon-bullhorn" 2450) (badge "icon-heart" 200)]])

(defn user-header [user]
  [:div.row (user-username user) (user-badges user)])


;;Details
(defn field [name value]
  (when (not (empty? value))
    [:span [:strong name] [:br ] [:span value] [:br ] [:br ]]))

(defn phone [{:keys [phone]}]
  (field "Phone" phone))

(defn website [{:keys [website]}]
  (when (not (empty? website))
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

;;Settings

(defn settings-form [user message button-text redirect_to errors]
  [:div.row-fluid [:div.span6.offset3 [:div.module [:h1 message]
                                       (user-form button-text :put (settings-url user) redirect_to user errors)]]])
(defn debug [value]
  (if env/debug? [:div.row.debug [:div {:class "span12.hide"} (str value)]]))

(defn- settings-page-template [user-result redirect_to header message button-text]
  (let [user (result/value user-result)
        errors (result/errors user-result)]
    (common/layout
      {:navbar {:search {:enabled false} :login {:enabled true}}}
      [:header.jumbotron.subhead [:div.container [:h1 header]]]
      (debug user)
      (debug errors)
      [:div.container-fluid (settings-form user message button-text redirect_to errors)])))

(defn signup-page [user-result redirect_to]
  (let [username (:username (result/value user-result))]
    (settings-page-template user-result redirect_to (str "Welcome " username) "Please tell us more about you:" "Continue")))

(defn settings-page [user-result redirect_to & [default-username]]
  (let [username (:username (result/value user-result) default-username)]
    (settings-page-template user-result redirect_to (str username) "Update your profile:" "Update")))