(ns greendroids.core)

(defn clj->js
 "Recursively transforms ClojureScript maps into Javascript objects,
  other ClojureScript colls into JavaScript arrays, and ClojureScript
  keywords into JavaScript strings."
 [x]
 (cond
  (string? x) x
  (keyword? x) (name x)
  (map? x) (.-strobj (reduce (fn [m [k v]]
                               (assoc m (clj->js k) (clj->js v))) {} x))
  (coll? x) (apply array (map clj->js x))
  :else x))


(def data (atom {}))
(def gee (atom nil))
(def ctx (atom nil))

(defn circle
  "Draws a filled circle to x, y with radius rad."
  [x y rad]
  (doto @ctx
   (.beginPath)
   (.arc x y rad rad 0 (* 2.0 Math/PI) true)
   (.closePath)
   (.fill)))

(defn line [x1 y1 x2 y2]
  (.beginPath @ctx)
  (.moveTo @ctx x1 y1)
  (.lineTo @ctx x2 y2)
  (.closePath @ctx)
  (.stroke @ctx))

(defn draw []
  (let [width (. @gee -width)
        height (. @gee -height)]
    (set! (. @ctx -fillStyle) "rgb(0, 0, 0)")
    (.fillRect @ctx 0 0 width height))
  (set! (. @ctx -fillStyle) "rgb(255, 255, 255)")
  (set! (. @ctx -textAlign) "left")
  (set! (. @ctx -textBaseline) "middle")
  (set! (. @ctx -font) "20pt Courier New")
  (.fillText @ctx (str "fps " (Math/round (. @gee -frameRate))) 50 40))

(defn move
  "Callback for when the user moves."
  [])

(defn stopshooting
  "Callback for when the user stops shooting."
  [])

(defn shoot
  "Callback for when the user shoots."
  [])

(defn ^:export start []
  (let [GEE (. js/window -GEE)
        params (clj->js {:fullscreen true
                         :context "2d"})]
    
    (swap! gee (fn [] (new GEE params))))
  (swap! ctx (fn [] (. @gee -ctx)))
  (set! (. @gee -draw) draw)
  (set! (. @gee -mousemove) move)
  (set! (. @gee -mousedown) shoot)
  (set! (. @gee -mouseup) stopshooting)
  (set! (. @gee -mousedrag) move)
  (.appendChild (. js/document -body)
                (. gee -domElement)))