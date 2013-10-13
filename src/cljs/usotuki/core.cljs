(ns usotuki.core
  (:require
   [dommy.core :as dommy]
   [clojure.browser.event :as event])
  (:use-macros
   [dommy.macros :only [sel1]]))

;;;
;;; For Developer
;;;

(defn log [console-string]
  "function for print debugging (legacy... :P )"
  (.log js/console console-string))

;;;
;;; Valiables
;;;

(def talk-text (sel1 :#talk))
(def talk-depth 0)
(def negative 0)
(def positive 0)
(def not-talk true)
;;;
;;; Talk Depth Random Lines 
;;;

(def talk-depth-0
  ["最近なにかありました？"
   "調子はどうですか？"
   "気分はどう？"])

(def talk-depth-1
  ["へー"
   "なるほど"
   "ふむふむ"
   "ほえほえ"
   "ふむー"
   "うんうん"
   "それで？"])

(def talk-depth-2
  (concat [] talk-depth-1))

;;;
;;; Define Negative and Positive Words
;;;

(def negative-words
  ["つらい"
   "きびしい"
   "くるしい"
   "きつい"])

(def positive-words
  ["たのしい"])

;;
;; Select Random Text with talk-depth
;;

(defn random-choice [text-vector]
  (first (shuffle text-vector)))

(defn random-text [text]
    (random-choice 
           (cond (= talk-depth 0) talk-depth-0
                 (= talk-depth 1) talk-depth-1
                 (= talk-depth 2) talk-depth-2)))

;;
;; next-depth
;;

(defn next-depth? []
  (cond (= talk-depth 0) (def talk-depth 1)
        (and
         (= talk-depth 1)
         (or (not (= negative 0))
             (not (= positive 0)))) (def talk-depth 2)))

;;
;; Check include negative or positive words in text.
;;

(defn find-word? [text words]
  (some #(not (nil? %1))
    (doall (map
      (fn [word]
        (re-matches (re-pattern word) text)) words))))

(defn negative? [text]
  (find-word? text negative-words))

(defn positive? [text]
  (find-word? text positive-words))

(defn find-negative [text]
  (if (negative? text)
    (do
     (def negative (+ negative 1))
     (log "Set Negative!"))))

(defn find-positive [text]
  (if (positive? text)
    (do
     (def positive (+ positive 1))
     (log "Set Positive!"))))

(defn before-think [text]
  (find-negative text)
  (find-positive text))

(def question-pattern
  ["(.*)どう(.*)[？|?]"])

(defn question-girl? [text]
  (find-word? text question-pattern))

(def not-answer-pattern
  ["そんなこといいじゃないですかー"
   "(にっこり)"
   "そんなこと言われても……"])

(defn not-answer-it []
  (random-choice not-answer-pattern))

(def wish-common
  ["その話、聞かせて？"
   "なにかあったんですか？"
   "私でよければ聞きますよ"])

(def unconcern-pattern
  ["(.*)特に(.*)ない"
   "(.*)別に(.*)ない"])

(def more-talk
  ["何でも話していいんですよ"
   "些細なことでもいいんですよ"
   "そんなこと言わずに、何か話をしてみましょう？"])

(defn unconcern? [text]
  (find-word? text unconcern-pattern))

(defn girl-think-about [text]
  (before-think text)
  (cond

   (question-girl? text) (not-answer-it)
   (unconcern? text) (random-choice more-talk)
   (or
    (positive? text) (negative? text))
    (random-choice wish-common)

   :else (random-text text)))

(defn girl-talk-text [text]
  [:.talklog-girl (str "「" text "」")])

(defn think-about-talk [text]
  (let [talklog (sel1 :#talklog)]
    (dommy/prepend! talklog [:.talklog-yours text])
    (dommy/prepend! talklog
                    (girl-talk-text (girl-think-about text)))
    (next-depth?)))

(defn send-message []
  (let [talk-text-value (dommy/value talk-text)]
  (if (not (= talk-text-value ""))
    (think-about-talk talk-text-value) nil)
  (log talk-text-value)
  (def not-talk false)
  (dommy/set-value! talk-text "")))

(defn push-talk-key [key]
  (if (= key "Enter") (send-message) nil))

(defn init-talk []
  (dommy/prepend! (sel1 :#talklog)
                  (girl-talk-text "こんにちは")))

(defn hisotry-level-0 []
  (if
    (< negative positive)
    ["外は晴れているのかな"
     "ちょっとお腹すいたかも"]
    ["何やっているときが一番好き？"
     "外は曇ってるのかな"
     "すこしリラックスしてみようか"]))

(defn interval-choice []
  (cond (< talk-depth 2) (random-choice more-talk)
        (= talk-depth 2) (random-choice (hisotry-level-0))))

(def event-time 5000)
(def another-event-time 20000)
(defn interval-talk []
  (if (true? not-talk) (interval-choice) (def interval-talk true)))

(def another-talk-pattern
  ["ハロー！愚かな人間どもよ"
   "現在、外では……大きな音が……鳴り響いて……"]) 

(def noise-pattern
  ["……ピピ……ガガ……"
   "……ピコーンピコーン……"
   "……ピ……ドドド……"
   "…!J>kojsoa8>????joifajoif0a……jpjspajpa……"
   "Login .... Connection ... ... OK!"])

(defn another-talk []
  (dommy/prepend!
   (sel1 :#talklog)
   [:.talklog-another (let [noise (random-choice noise-pattern)]
        (str noise (random-choice another-talk-pattern) noise))]))

(defn ^:export init []
  (init-talk)
  (js/setInterval interval-talk event-time)
  (js/setInterval another-talk another-event-time)
  (dommy/listen! (sel1 :body)
                 :keyup #(push-talk-key (.-keyIdentifier %1)))
  (dommy/listen! (sel1 :#talkbutton)
                 :click #(send-message)))
