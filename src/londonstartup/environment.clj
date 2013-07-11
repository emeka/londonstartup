(ns londonstartup.environment)

(def debug? (= "true" (get (System/getenv) "DEBUG" )))
