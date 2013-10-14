(ns usotuki.common)

(defn random-choice [text-vector]
  (first (shuffle text-vector)))

(defn find-word? [text words]
  (some #(not (nil? %1))
    (doall (map
      (fn [word]
        (re-matches (re-pattern word) text)) words))))


