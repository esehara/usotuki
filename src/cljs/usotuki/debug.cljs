(ns usotuki.debug)

(defn log [console-string]
  "function for print debugging (legacy... :P )"
  (.log js/console console-string))

