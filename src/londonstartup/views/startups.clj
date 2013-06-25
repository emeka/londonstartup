(ns londonstartup.views.startups
  (:require [noir.validation :as validate]
            [clojure.string :as string]
            [londonstartup.views.common :as common]
            [londonstartup.views.bootstrap :as bs])
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

(defn id [{:keys [_id]} & suffix]
  (let [prefix (str "startup-" _id)
        suffix (first suffix)]
    (if suffix
      (str prefix "-" suffix)
      prefix)))

(defn idref [startup & suffix]
  (str "#" (id startup (first suffix))))

;;Forms
(defn error-text [errors]
  [:span (string/join "" errors)])

(defn startup-fields [{:keys [website name _id]}]
  ;(validate/on-error :name error-text)
  (label :name-label "Name: ")
  (text-field {:placeholder "Name"} :name name)
  ;(validate/on-error :website error-text)
  (label :website-label "Website:")
  (text-field {:placeholder "Website"} :website website)
  (hidden-field :_id _id))

(defn startup-remove-form [{:keys [website]}]
  (when website
    (form-to [:delete (str "/startup/" website)] ;The url should be calculated from the route
      (submit-button {:class "submit"} "Delete"))))

(defn startup-form [action method url startup]
  (form-to [method url]
    (startup-fields startup)
    (submit-button {:class "submit"} action))
  (startup-remove-form startup))

;;Header
(defn badge [icon value]
  [:small (bs/icon icon) " " value " "])

(defn startup-name [{:keys [name] :as startup}]
  [:div.startup-header.span6 [:h2 [:a {:href (idref startup "details") :data-toggle "collapse"} name]]])

(defn startup-badges [startup]
  [:div.startup-badges.span4.offset2 [:h2 (badge "icon-user" 13) (badge "icon-bullhorn" 2450) (badge "icon-heart" 200)]])

(defn startup-header [startup]
  [:div.row (startup-name startup) (startup-badges startup)])

;;Details
(defn location [{:keys [name website] :as startup}]
  (str "<address class=\"span6\">
                      <strong>" name "</strong><br>
                      Belgrave House<br>
                      76 Buckingham Palace Rd<br>
                      London<br>
                      Greater London<br>
                      SW1W 9TQ<br><br>

                      <strong>Phone</strong><br>‎
                      020 7031 3000<br><br>

                      <strong>Website</strong><br>
                      <a href=\"" website "\">" (string/replace website "http://" "") "</a>
                  </address>
                  <div class=\"span6\">
                      <iframe width=\"300\" height=\"300\" frameborder=\"0\" scrolling=\"no\" marginheight=\"0\" marginwidth=\"0\"
                              src=\"https://maps.google.co.uk/maps?f=q&amp;source=s_q&amp;hl=en&amp;geocode=&amp;q=Google+London,+Buckingham+Palace+Road,+London&amp;aq=0&amp;oq=google+lon&amp;sll=51.48931,-0.08819&amp;sspn=1.061176,1.253815&amp;ie=UTF8&amp;hq=Google+London,+Buckingham+Palace+Road,+London&amp;t=m&amp;ll=51.494904,-0.146427&amp;spn=0.008016,0.012875&amp;z=15&amp;iwloc=near&amp;output=embed\"></iframe>
                      <br/>
                      <small><a
                              href=\"https://maps.google.co.uk/maps?f=q&amp;source=embed&amp;hl=en&amp;geocode=&amp;q=Google+London,+Buckingham+Palace+Road,+London&amp;aq=0&amp;oq=google+lon&amp;sll=51.48931,-0.08819&amp;sspn=1.061176,1.253815&amp;ie=UTF8&amp;hq=Google+London,+Buckingham+Palace+Road,+London&amp;t=m&amp;ll=51.494904,-0.146427&amp;spn=0.008016,0.012875&amp;z=15&amp;iwloc=A\"
                              >View Larger Map</a></small>
                  </div>")
  )

(defn update [{:keys [name website] :as startup}]
  (str "<div class=\"span12\">
                      <h2>Update</h2>

                      <form>
                          <div class=\"control-group\">
                              <label class=\"control-label\" for=\"startupName\">Name</label>

                              <div class=\"controls\">
                                  <input type=\"text\" id=\"startupName\" placeholder=\"Startup Name\"
                                         value=\"" name "\">
                              </div>
                          </div>
                          <div class=\"control-group\">
                              <label class=\"control-label\" for=\"startupWebsite\">Website</label>

                              <div class=\"controls\">
                                  <input type=\"text\" id=\"startupWebsite\" placeholder=\"Startup Website\"
                                         value=\"" website "\">
                              </div>
                          </div>
                          <button type=\"submit\" class=\"btn\" data-toggle=\"collapse\" data-target=\"#startup-1-edit\">
                              Cancel
                          </button>
                          <button type=\"submit\" class=\"btn btn-danger\">Submit</button>
                      </form>
                  </div>")
  )

(defn startup-details [{:keys [_id] :as startup}]
  (bs/tabs (id startup "details")
    {:id (id startup "location") :tab (bs/icon "icon-map-marker") :content (location startup)}
    {:id (id startup "edit") :tab (bs/icon "icon-edit") :content (update startup)}
    ))

(defn startup-item [{:keys [_id website name] :as startup}]
  (when startup
    [:section.startup.container-fluid {:id (id startup)}
     (startup-header startup) (startup-details startup)]))

(defn startup-list [startups]
  (map #(startup-item %) startups))

;; Pages
(defn startup-page [{:keys [website] :as startup}]
  (common/layout
    [:div (startup-item startup)]
    [:div (startup-form "Update" :put (str "/startup/" website) startup)])) ;The url should be calculated from the route

(defn startups-page [new-startup startups]
  (common/layout
    [:header.jumbotron.subhead [:div.container [:h1 "London Startup Directory"]
                                [:p.lead "The open startup reference in London."]]]
    ;(startup-form "Add" :post "/startups" new-startup) ;The url should be calculated from the route
    (startup-list startups)
    ))
