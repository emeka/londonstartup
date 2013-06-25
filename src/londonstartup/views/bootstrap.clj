(ns londonstartup.views.bootstrap
  (:use [hiccup.page :only [include-css include-js html5]]
        [hiccup.def :only [defhtml]]
        [hiccup.element :only [link-to]]))


(defn icon [name]
  [:i {:class name}])

(defn tab-headers [definitions]
  (if (not (empty? definitions))
     (reduce #(conj %1 [(if (= 1 (count %1)) :li.active :li) [:a {:href (str "#"(:id %2)) :data-toggle "tab"} (:tab %2)]]) [:ul.nav.nav-tabs] definitions)))

(defn tab-panes [definitions]
  (if (not (empty? definitions))
     (reduce #(conj %1 [(if (= 1 (count %1)) :div.tab-pane.active :div.tab-pane) {:id (:id %2)} (:content %2)]) [:div.tab-content] definitions)))

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