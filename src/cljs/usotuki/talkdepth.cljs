(ns usotuki.talk-depth
  (:require
   [usotuki.debug :as debug]
   [usotuki.common :as common]))

(def talk-depth 0)
(def negative 0)
(def positive 0)

(defn ^:export debug-talk-depth []

  "デバッグ用関数"
  
  (debug/log talk-depth))

(def wish-common
  ["その話、聞かせて？"
   "それ、少し興味があります"
   "なにかあったんですか？"
   "私でよければ聞きますよ"])

(def talk-depth-0
  
  ^{:doc "シーン0: 最初のトーク"}

  ["最近なにかありました？"
   "調子はどうですか？"
   "気分はどう？"])

(def talk-depth-1

  ^{:doc "シーン1: 相槌をうち、話を聞いている"}
  
  (concat ["へー" "なるほど" "ふむふむ" "ほえほえ" "ふむー" "ふーん" "うんうん"
   "それで？"] wish-common))

(def talk-depth-2

  ^{:doc "シーン2: 感情を見せる"}
  
  (concat ["いろいろありますもんね" ""] talk-depth-1))

(def talk-depth-3

  ^{:doc "シーン3: 人工少女のトリガー"}
  
  (concat [] talk-depth-2))

(def trigger-depth-3 ["薬" "病" "妄想" "治療" "母"])

(defn random-text [text]
    (common/random-choice 
           (cond (= talk-depth 0) talk-depth-0
                 (= talk-depth 1) talk-depth-1
                 (= talk-depth 2) talk-depth-2
                 (= talk-depth 3) talk-depth-3)))


(defn next? [text]
  "

  次のシーンに向かうべきかどうかを判断するための関数です

  :シーンの関係図:

  [0] -> [1] -> [2] -> [3]

"

  (cond
   
   ;; シーン0 -> 1
   ;; シーン0は、単なる導入なので、一言返事をすれば終了する

   (= talk-depth 0) (def talk-depth 1)

   ;; シーン1 -> 2
   ;; シーン1は、何らかの感情語を使用するとシーン2に移行する

   (and
    (= talk-depth 1)
    (or (not (= negative 0))
        (not (= positive 0)))) (def talk-depth 2)

   ;; シーン2 -> [シーン3]

        (= talk-depth 2) (talk-depth-node-2 text)))

(defn talk-depth-node-2 [text]
  "
  シーン2からの移行をを判定します
  "
  (cond
   ;; トリガー語が発せられた
   ;; シーン3へ
   (common/find-word?
    text (map #(str "(.*)" %1 "(.*)") trigger-depth-3)) (def talk-depth 3)))
