(defproject usotuki "0.1.0-SNAPSHOT"
  :description "Usotuki -- Artifact Girl"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj"]
  :cljsbuild {:builds
              [{:source-paths ["src/cljs/usotuki/"]
                :compiler
                {:output-to "resources/js/girl.js"
                 :optimizations :whitespace
                 :pretty-print false}}]}
  :plugins [[lein-cljsbuild "0.3.3"]]
  :dependencies [
                 [prismatic/dommy "0.1.1"]
                 [org.clojure/clojure "1.5.1"]])
