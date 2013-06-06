(ns londonstartup.views.startups
  (:require [noir.validation :as validate]
            [clojure.string :as string]
            [londonstartup.views.common :as common])
  (:use hiccup.core
        hiccup.element
        hiccup.page
        hiccup.form))

;;Validation
;(defn valid? [{:keys [website name]}]
;  (validate/rule (validate/has-value? website)
;    [:website "A startup must have a website"])
;  (validate/rule (validate/has-value? name)
;    [:name "A startup must have a name"])
;  ;(not (validate/errors? :website :name)) ;;TODO: valid? should be at view level.
;  )

;; Page Elements
(defn error-text [errors]
  (html
    [:span (string/join "" errors)]))

(defn startup-form-field [{:keys [website name _id]}]
  (html
    (validate/on-error :name error-text)
    (text-field {:placeholder "Name"} :name name)
    (validate/on-error :website error-text)
    (text-field {:placeholder "Website"} :website website)
    (hidden-field :_id _id)))

(defn startup-form [action method url startup]
  (html
    (form-to [method url]
      [:ul.actions [:li (link-to {:class "submit"} "/" action)]]
      (startup-form-field startup)
      (submit-button {:class "submit"} action))))

(defn startup-remove-form [{:keys [website]}]
  (html
    (when website
      (form-to [:delete (str "/startup/" website)] ;The url should be calculated from the route
        (submit-button {:class "submit"} "Delete")))))

(defn startup-item [{:keys [website name] :as startup}]
  (html
    (when startup
      [:dl.startup [:dt "Company Name:"]
       [:dd (link-to (str "/startup/" website) name)] ;The url should be calculated from the route
       (startup-remove-form startup)
       [:dt "Website:"]
       [:dd website]])))

;; Pages
(defn startup-page [{:keys [website] :as startup}]
  (html
    (common/layout
      [:div (startup-item startup)]
      [:div (startup-form "Update" :put (str "/startup/" website) startup)]))) ;The url should be calculated from the route

(defn startups-page [startups]
  (html
    (common/layout
      [:h1 "London Startup Directory"]
      (startup-form "Add" :post "/startups" {})
      [:ul.startups (map #(conj [:li ] (startup-item %)) startups)]))) ;The url should be calculated from the route
