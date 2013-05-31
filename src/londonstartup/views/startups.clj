(ns londonstartup.views.startups
  (:require [londonstartup.views.common :as common]
            [londonstartup.models.startup :as startup]
            [noir.content.getting-started]
            [noir.validation :as validate]
            [noir.response :as resp]
            [clojure.string :as string])
  (:use noir.core
        hiccup.core
        hiccup.element
        hiccup.page
        hiccup.form
        ))

(defpartial error-text [errors]
  [:span (string/join "" errors)])

(defpartial startup-fields [{:keys [website name]}]
  (validate/on-error :name error-text)
  (text-field {:placeholder "Name"} :name name)
  (validate/on-error :website error-text)
  (text-field {:placeholder "Website"} :website website)
  )

(defpartial startup-item [{:keys [website name] :as startup}]
  (when startup
    [:li [:dl [:dt name]
          [:dd [:ul [:li website]]]]]))

(defpartial startup-list [startups]
  (common/layout
    [:ul (map startup-item startups)]))

(defpage "/startups" []
  (startup-list (startup/startups))
  )

(defpage "/startup/:website" {:keys [website]}
  (startup-item (startup/website->startup website))
  )

(defpage "/add/startup" {:as startup}
  (common/layout
    (form-to [:post "/add/startup"]
      [:ul.actions [:li (link-to {:class "submit"} "/" "Add")]]
      (startup-fields startup)
      (submit-button {:class "submit"} "add startup"))))

(defpage [:post "/add/startup"] {:keys [name website] :as new-startup}
  (if (startup/add! new-startup)
    (resp/redirect (url-for "/startup/:website" {:website (get new-startup :website)}))
    (render "/add/startup" new-startup)
    )
  )