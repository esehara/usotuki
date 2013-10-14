(ns usotuki.core
  (:use
   [usotuki.debug :only [log debug-talk-depth]]
   [usotuki.common :only [random-choice find-word?]]
   [usotuki.another-talk :only [another-talk]])
  (:require
   [usotuki.talk-depth :as depth]
   [usotuki.words :as words]
   [dommy.core :as dommy]
   [clojure.browser.event :as event])
  (:use-macros
   [dommy.macros :only [sel1]]))

(def talk-text (sel1 :#talk))
(def not-talk true)

(defn negative? [text] (find-word? text words/negative))
(defn positive? [text] (find-word? text words/positive))

(defn find-negative [text]
  (if (negative? text)
    (do
     (def negative (+ talkdepth/negative 1
                      ))
     (log "Set Negative!"))))

(defn find-positive [text]
  (if (positive? text)
    (do
     (def positive (+ talkdepth/positive 1))
     (log "Set Positive!"))))

(defn before-think [text]
  (find-negative text)
  (find-positive text))

(def question-pattern-not-fix
  ["(.*)どう(.*)"
   "(.*)何か(.*)"
   (str "(.*)" words/yours "は(.*)[なの|かい](.*)[なの|かい](.*)")])

(def question-pattern
  (map #(str %1 words/last-fix-question) question-pattern-not-fix))

(defn question-girl? [text]
  (find-word? text question-pattern))

(def not-answer-pattern
  ["そんなこといいじゃないですかー"
   "(にっこり)"
   "そんなこと言われても……"])

(defn not-answer-it []
  (random-choice not-answer-pattern))

(def unconcern-pattern
  ["(.*)[特に|とくに|別に|べつに|何も|なにも](.*)ない(.*)"])

(def more-talk
  ["何でも話していいんですよ"
   "些細なことでもいいんですよ"
   "そんなこと言わずに、何か話をしてみましょう？"])

(def you-are-good
  ["素敵なかただと思っていますよ、きっと"
   "いいと思っていますよ"
   "私は嫌いじゃないですよ"])

(def dom-talklog (sel1 :#talklog))

(def im-pattern
  [(str "(.*)" words/im "(.*)思う(.*)")])

(defn unconcern? [text]
  (find-word? text unconcern-pattern))

(defn girl-think-about-me? [text]
  (find-word? text im-pattern))

(defn girl-think-about [text]
  (before-think text)
  (cond
   (question-girl? text) (not-answer-it)
   (unconcern? text) (random-choice more-talk)
   (or (positive? text) (negative? text)) (random-choice depth/wish-common)
   (girl-think-about-me? text) (random-choice you-are-good) 
   :else (depth/random-text text)))

(defn girl-talk-text [text] [:.talklog-girl (str "「" text "」")])
(defn talklog-prepend! [dom] (dommy/prepend! dom-talklog dom))

(defn think-about-talk [text]
  (let [talklog dom-talklog]
    (talklog-prepend! [:.talklog-yours text])
    (talklog-prepend! (girl-talk-text (girl-think-about text)))
    (depth/next? text)))

(defn send-message []
  (let [talk-text-value (dommy/value talk-text)]
  (if (not (= talk-text-value ""))
    (think-about-talk talk-text-value) nil)
  (def not-talk false) (def interval-talk false)
  (dommy/set-value! talk-text "")))

(defn push-talk-key [key] (if (= key "Enter") (send-message) nil))

(defn init-talk []
  (dommy/prepend! dom-talklog (girl-talk-text "こんにちは")))

(defn hisotry-level-0 []
  (if
    (< negative positive)
    ["外は晴れているのかな" "ちょっとお腹すいたかも"]
    ["何やっているときが一番好き？"
     "少し背筋を伸ばしてみましょう"
     "外は曇ってるのかな"
     "すこしリラックスしてみようか"]))

(defn interval-choice []
  (cond (< talkdepth/talk-depth 2) (random-choice more-talk)
        (= talkdepth/talk-depth 2) (random-choice (hisotry-level-0))))

(def event-time 5000)
(def another-event-time 60000)
(def flashback-time 60000)
(defn interval-talk []
  (if (true? not-talk) (interval-choice) (def interval-talk true)))

(def flashback-list
  ["暗い闇のなかに閉じ込められたことがありますか？"
   "わけのわからない薬を飲まされそうになったから、机の中に隠してたの"
   "多くの人々が言っていることがよくわからなくなる"
   "なんだろう、世の中のことが無関心になって、こう、ふわっと"])

(def negative-emotion-list
  ["殺してやる！って思ったことありませんか"
   "たまにね、神様の声が聞こえてくるの、耳からね"
   "たまに、私はどこか壊れちゃったのかなと思う"
   "消えてなくなりたい"])

(defn emotion-select []
  (cond (> negative 2) (dommy/prepend! dom-talklog
                                      (girl-talk-text (random-choice negative-emotion-list)))))

(defn flashback-talk []
  (let [select-branch (rand-int 30)]
  (cond (< select-branch 10) (emotion-select)
         :else (cond (= talkdepth/talk-depth 2) (dommy/prepend! dom-talklog
                                (girl-talk-text (random-choice flashback-list)))))))


(defn ^:export init []
  (init-talk)
  (js/setInterval interval-talk event-time)
  (js/setInterval another-talk another-event-time)
  (js/setInterval flashback-talk flashback-time)
  (dommy/listen! (sel1 :body)
                 :keyup #(push-talk-key (.-keyIdentifier %1)))
  (dommy/listen! (sel1 :#talkbutton)
                 :click #(send-message)))
