(ns londonstartup.views.bootstrap
  (:use [hiccup.page :only [include-css include-js html5]]
        [hiccup.def :only [defhtml defelem]]
        [hiccup.element :only [link-to]]
        [hiccup.form]))


(defn icon [name]
  [:i {:class name}])

(defn tab-headers [definitions]
  (if (not (empty? definitions))
    (reduce #(conj %1 [(if (= 1 (count %1)) :li.active :li ) [:a {:href (str "#" (:id %2)) :data-toggle "tab"} (:tab %2)]]) [:ul.nav.nav-tabs ] definitions)))

(defn tab-panes [definitions]
  (if (not (empty? definitions))
    (reduce #(conj %1 [(if (= 1 (count %1)) :div.tab-pane.active :div.tab-pane ) {:id (:id %2)} (:content %2)]) [:div.tab-content ] definitions)))

(defn tabs [id & definitions]
  "The definitions is a list of maps.  Each map contains three elements:
   id: the tab id,
   tab: the tab header which can be an icon,
   content: the tab content being a hiccup vector,
   for example {:id \"startup1-location\" :tab (icon \"icon-map-marker\" :content [:div]) }"
  (if (not (empty? definitions))
    [:div.tabbable.collapse {:id id}
     (tab-headers definitions)
     (tab-panes definitions)
     ]
    ))

(defn row [& content]
  [:div.row-fluid content])

(defn span [size & content]
  [:div {:class (str "span" size)} content])

(defn span1 [& content] (span 1 content))
(defn span2 [& content] (span 2 content))
(defn span3 [& content] (span 3 content))
(defn span4 [& content] (span 4 content))
(defn span5 [& content] (span 5 content))
(defn span6 [& content] (span 6 content))
(defn span7 [& content] (span 7 content))
(defn span8 [& content] (span 8 content))
(defn span9 [& content] (span 9 content))
(defn span10 [& content] (span 10 content))
(defn span11 [& content] (span 11 content))
(defn span12 [& content] (span 12 content))

(defelem control-group [id name & content]
  [:div.control-group (label {:class "control-label"} id name)
   [:div.controls content]])
