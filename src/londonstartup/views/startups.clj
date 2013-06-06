(ns londonstartup.views.startups
  (:require [londonstartup.views.common :as common]
            [londonstartup.models.startup :as startup]
            [noir.content.getting-started]
            [noir.validation :as validate]
            [noir.response :as resp]
            [clojure.string :as string]
            [londonstartup.common.result :as result])
  (:use noir.core
        hiccup.core
        hiccup.element
        hiccup.page
        hiccup.form)
  (import org.bson.types.ObjectId))

;;Validation
;(defn valid? [{:keys [website name]}]
;  (validate/rule (validate/has-value? website)
;    [:website "A startup must have a website"])
;  (validate/rule (validate/has-value? name)
;    [:name "A startup must have a name"])
;  ;(not (validate/errors? :website :name)) ;;TODO: valid? should be at view level.
;  )

;; Page Elements
(defpartial error-text [errors]
  [:span (string/join "" errors)])

(defpartial startup-form-field [{:keys [website name _id]}]
  (validate/on-error :name error-text)
  (text-field {:placeholder "Name"} :name name)
  (validate/on-error :website error-text)
  (text-field {:placeholder "Website"} :website website)
  (hidden-field :_id _id))

(defpartial startup-form [action method url startup]
  (form-to [method url]
    [:ul.actions [:li (link-to {:class "submit"} "/" action)]]
    (startup-form-field startup)
    (submit-button {:class "submit"} action)))

(defpartial startup-remove-form [{:keys [website]}]
  (when website
    (form-to [:delete (url-for startup-remove {:website website})]
      (submit-button {:class "submit"} "Delete"))))

(defpartial startup-item [{:keys [website name] :as startup}]
  (when startup
    [:dl [:dt "Company Name:"]
     [:dd (link-to (url-for startup {:website website}) name)]
     (startup-remove-form startup)
     [:dt "Website:"]
     [:dd website]]))

;; Pages
(defpartial startup-page [{:keys [website] :as startup}]
  (common/layout
    [:div (startup-item startup)]
    [:div (startup-form "Update" :put (url-for startup-update {:_website website}) startup)]))

(defpartial startups-page [startups]
  (common/layout
    (startup-form "Add" :post (url-for startup-new) {})
    [:ul (map #(conj [:li ] (startup-item %)) startups)]))

;; Routing
(defpage startups "/startups" []
  (startups-page (result/value (startup/startups))))

(defpage startup "/startup/:website" {:keys [website]}
  (startup-page (result/value (startup/website->startup website))))

(defpage startup-new [:post "/startups"] {:keys [website] :as new-startup}
  (if (not (result/has-error? (startup/add! new-startup)))
    (resp/redirect (url-for "/startup/:website" {:website website}))
    (render startups)))

(defpage startup-update [:put "/startup/:_website"] {:keys [website _id] :as updated-startup}
  (if (not (result/has-error? (startup/update! (dissoc (merge updated-startup {:_id (ObjectId. _id)}) :_method, :_website))))
    (resp/redirect (url-for "/startup/:website" {:website website}))
    (render startup updated-startup)))

(defpage startup-remove [:delete "/startup/:website"] {:keys [website]}
  (result/value (startup/remove-website! website))
  (resp/redirect (url-for "/startups")))
