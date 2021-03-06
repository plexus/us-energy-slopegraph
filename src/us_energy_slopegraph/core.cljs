(ns us-energy-slopegraph.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljsjs.d3]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload
(def w 450)
(def h 400)
(def column-space 300)


(defonce svg (..
          (.select js/d3 "#slopegraph")
          (append "svg")
          (attr "height" h)
          (attr "width" w)))

(defonce height-scale
  (.. js/d3
      (scaleLinear)
      (domain #js [0 1])
      (range #js [(- h 15) 2])))

(defn format-fuel-name [fuel-name]
  (->>
   (name fuel-name)
   (clojure.string/capitalize)
   (#(clojure.string/replace % #"-" " "))
  ))

(defn draw-header [years]
    (.. svg
        (selectAll "text.slopegraph-header")
        (data (into-array years))
        (enter)
        (append "text")
        (classed "slopegraph-header" true)
        (attr "x" (fn [d i] (+ 10 (* i column-space)) ))
        (attr "y" 15)
        (text #(str %))
    ))

(defn column1 [data]
  (.. svg
      (selectAll "text.slopegraph-column-1")
      (data (into-array data))
      (enter)
      (append "text")
      (classed "slopegraph-column" true)
      (classed "slopegraph-column-1" true)
      (attr "x" 10)
      (attr "y" #(height-scale (val %)))
      (text #((.format js/d3 ".2%") (val %)))))

(defn column2 [data]
  (.. svg
      (selectAll "text.slopegraph-column-2")
      (data (into-array data))
      (enter)
      (append "text")
      (classed "slopegraph-column" true)
      (classed "slopegraph-column-2" true)
      (attr "x" column-space)
      (attr "y" #(height-scale (val %)))
      (text #(str
              ((.format js/d3 ".2%") (val %))
              " "
              (format-fuel-name (key %))))
      ))


(defn draw-line [data-col-1 data-col-2]
  (.. svg
      (selectAll "line.slopegraph-line")
      (data (into-array data-col-1))
      (enter)
      (append "line")
      (classed "slopegraph-line" true)
      (attr "x1" 55)
      (attr "x2" (- column-space 5))
      (attr "y1" #(height-scale (val %)))
      (attr "y2" #(height-scale ((key %) data-col-2)))
      ))


(defn draw-slopegraph [data]
  (let [data-2005 (:values (first (filter #(= 2005 (:year %)) data)))
        data-2015 (:values (first (filter #(= 2015 (:year %)) data)))]

    (draw-header [2005 2015])
    (column1 data-2005)
    (column2 data-2015)
    (draw-line data-2005 data-2015)
    ))

;; Drawing our slopegraph

(def data
  [{
    :year 2005,
    :values {
             :natural-gas 0.2008611514256557,
             :coal 0.48970650816857986,
             :nuclear 0.19367190804075465
             }
    }

   {
    :year 2015,
    :values {
             :natural-gas 0.33808321253456974,
             :coal 0.3039492492908485,
             :nuclear 0.1976276775179704
             }
    }])

(draw-slopegraph data)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
