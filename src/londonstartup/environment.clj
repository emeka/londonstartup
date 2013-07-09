(ns londonstartup.environment)

(def debug? (not (nil? (get (System/getenv) "DEBUG" ))))
