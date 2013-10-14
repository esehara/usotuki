(ns usotuki.another-talk)

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
   "Login .... Connection ... ... OK! ... ..."
   "……ザー……ザー……"])

(defn another-talk []
  (dommy/prepend!
   (sel1 :#talklog)
   [:.talklog-another (let [noise (random-choice noise-pattern)]
        (str noise (random-choice another-talk-pattern) noise))]))
