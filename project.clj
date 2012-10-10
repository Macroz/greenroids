(defproject greenroids "0.1.0-SNAPSHOT"
  :description "GreenRoids - Droid shooting game for ClojuTRE"
  :url "http://greenroids.heroku.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "1.1.1"]
                 [hiccup "1.0.1"]
                 [ring "1.1.6"]]
  :plugins [[lein-cljsbuild "0.2.7"]
            [lein-ring "0.7.3"]]
  :profiles {:dev {:dependencies [[ring-mock "0.1.3"]]}}
  :ring {:handler greenroids.core/app}
  :cljsbuild {
              :builds [{
                        :source-path "src-cljs"
                        :compiler {
                                   :output-to "resources/public/js/cljs.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]})
