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
   "ふーん"
   "うんうん"
   "それで？"])

(def talk-depth-2
  (concat [] talk-depth-1))

;;;
;;; Define Negative and Positive Words
;;;

(def negative-words
  ["つらい" "辛い"
   "きびしい" "厳しい"
   "くるしい" "苦しい"
   "きつい"])

(def positive-words
  ["たのしい" "楽しい"
   "うれしい" "嬉しい"])

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

(def last-fix-question "[？|?]")

(def question-pattern-not-fix
  ["(.*)どう(.*)"
   "(.*)君は(.*)[なの|かい](.*)[なの|かい](.*)"])

(def question-pattern
  (map #(str %1 last-fix-question) question-pattern-not-fix))

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
   "それ、少し興味があります"
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
   (or (positive? text) (negative? text)) (random-choice wish-common)
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
     "少し背筋を伸ばしてみましょう"
     "外は曇ってるのかな"
     "すこしリラックスしてみようか"]))

(defn interval-choice []
  (cond (< talk-depth 2) (random-choice more-talk)
        (= talk-depth 2) (random-choice (hisotry-level-0))))

(def event-time 5000)
(def another-event-time 60000)
(def frashback-time 60000)
(defn interval-talk []
  (if (true? not-talk) (interval-choice) (def interval-talk true)))

(def frashback-list
  ["暗い闇のなかに閉じ込められたことがありますか？"
   "わけのわからない薬を飲まされそうになったから、机の中に隠してたの"
   "多くの人々が言っていることがよくわからなくなる"
   "なんだろう、世の中のことが無関心になって、こう、ふわっと"])

(def negative-emotion-list
  ["殺してやる！って思ったことありませんか"
   "たまにね、神様の声が聞こえてくるの、耳からね"])

(defn emotion-select []
  (cond (> negative 3)
        (dommy/prepend! (sel1 :#talklog)
                        (girl-talk-text (random-choice negative-emotion-list)))))

(defn frashback-talk []
  (let [select-branch (rand-int 30)]
   (cond (select-branch < 10) (emotion-select)
         :else (cond
                (= talk-depth 2)
                (dommy/prepend! (sel1 :#talklog)
                                (girl-talk-text (random-choice frashback-list)))))))

(def another-talk-pattern
  ["ハロー！愚かな人間どもよ"
   "あんた、今日の号外を見てないのか"
   "またビルから……飛び降りたぞ！！"
   "くそっ………何を考えてやがる……"
   "まさか……あんなことになるなんて……"
   "現在、外では……大きな音が……鳴り響いて……"]) 

(def noise-pattern
  ["……ピピ……ガガ……"
   "……ピコーンピコーン……"
   "……ピ……ドドド……"
   "…………"
   "** Remind Log **"
   "…!J>kojsoa8>????joifajoif0a……jpjspajpa……"
   "Login .... Connection ... ... OK!"
   "……ザー……ザー……"])

(defn another-talk []
  (dommy/prepend!
   (sel1 :#talklog)
   [:.talklog-another (let [noise (random-choice noise-pattern)]
        (str noise (random-choice another-talk-pattern) noise))]))

(defn ^:export init []
  (init-talk)
  (js/setInterval interval-talk event-time)
  (js/setInterval another-talk another-event-time)
  (js/setInterval frashback-talk frashback-time)
  (dommy/listen! (sel1 :body)
                 :keyup #(push-talk-key (.-keyIdentifier %1)))
  (dommy/listen! (sel1 :#talkbutton)
                 :click #(send-message)))
