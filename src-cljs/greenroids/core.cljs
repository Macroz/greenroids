(ns greenroids.core)

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

(defn make-droid []
  (let [x (rand-int 1000)
        y (rand-int 1000)
        position [x y]]
    {:position position}))

(def data (atom {:player {:position [100 100]}
                 :droids (repeatedly 10 make-droid)
                 :bullets []}))
(def gee (atom nil))
(def ctx (atom nil))

(defn circle
  "Draws a filled circle to x, y with radiuses radx and rady."
  [x y radx rady]
  (doto @ctx
    (.save)
    (.scale 1.0 (/ rady radx))
    (.beginPath)
    (.arc x (/ (* y radx) rady) radx 0 (* 2.0 Math/PI) true)
    (.closePath)
    (.fill)
    (.restore)))

(defn line
  "Draws a straight line between x1, y1 and x2, y2."
  [x1 y1 x2 y2]
  (.beginPath @ctx)
  (.moveTo @ctx x1 y1)
  (.lineTo @ctx x2 y2)
  (.closePath @ctx)
  (.stroke @ctx))

(defn normalize [vector]
  (let [[dx dy] vector
        length (Math/sqrt (+ (* dx dx) (* dy dy)))]
    [(/ dx length) (/ dy length)]))

(defn new-bullet [player-position player-direction]
  {:position player-position
   :direction (normalize player-direction)})

(defn move-bullet [bullet]
  (let [[px py] (bullet :position)
        [dx dy] (bullet :direction)
        new-position [(+ px (* dx 10))
                      (+ py (* dy 10))]]
    (assoc bullet :position new-position)))

(defn player-shoot []
  (let [player-position (get-in @data [:player :position])
        player-direction (get-in @data [:player :direction])
        new-bullets (conj (@data :bullets) (new-bullet player-position player-direction))]
    (swap! data (fn [data] (assoc data :bullets new-bullets)))))

(defn simulate
  "Simulates the world one step forward."
  []
  (let [[px py] (get-in @data [:player :position])
        [tx ty] (get-in @data [:player :target] [0 0])
        dx (* (- tx px) 0.01)
        dy (* (- ty py) 0.01)
        new-position [(+ px dx)
                      (+ py dy)]
        new-direction [dx dy]]
    (swap! data (fn [data] (assoc-in data [:player :position] new-position)))
    (swap! data (fn [data] (assoc-in data [:player :direction] new-direction)))
    (when (get-in @data [:player :shooting?])
      (player-shoot))
    (let [new-bullets (map move-bullet (@data :bullets))]
      (swap! data (fn [data] (assoc data :bullets new-bullets))))))


(defn draw
  "Draws the game."
  []
  (simulate)
  (let [width (. @gee -width)
        height (. @gee -height)]
    (set! (. @ctx -fillStyle) "rgb(0, 0, 0)")
    (.fillRect @ctx 0 0 width height))

  (set! (. @ctx -fillStyle) "rgb(0, 255, 0)")
  (doseq [d (@data :droids)]
    (let [[x y] (d :position)]
      (circle x y 10 10)))

  (set! (. @ctx -fillStyle) "rgb(255, 0, 0)")
  (set! (. @ctx -strokeStyle) "rgb(255, 255, 255)")
  (doseq [b (@data :bullets)]
    (let [[x y] (b :position)
          [dx dy] (b :direction)
          x2 (+ x (* dx 10))
          y2 (+ y (* dy 10))]
      (line x y x2 y2)))

  (set! (. @ctx -fillStyle) "rgb(255, 255, 255)")
  (let [[x y] (get-in @data [:player :position])]
    (circle x y 10 10))

  (let [[tx ty] (get-in @data [:player :target] [0 0])]
    (circle tx ty 5 5))

  (set! (. @ctx -fillStyle) "rgb(255, 255, 255)")
  (set! (. @ctx -strokeStyle) "rgb(255, 255, 255)")
  (set! (. @ctx -textAlign) "left")
  (set! (. @ctx -textBaseline) "middle")
  (set! (. @ctx -font) "20pt Courier New")
  (.fillText @ctx (str "fps " (Math/round (. @gee -frameRate))) 50 40))

(defn move
  "Callback for when the user moves."
  []
  (let [tx (. @gee -mouseX)
        ty (. @gee -mouseY)]
    (swap! data (fn [data] (assoc-in data [:player :target] [tx ty])))))

(defn stopshooting
  "Callback for when the user stops shooting."
  []
  (swap! data (fn [data] (assoc-in data [:player :shooting?] false))))

(defn shoot
  "Callback for when the user shoots."
  []
  (swap! data (fn [data] (assoc-in data [:player :shooting?] true))))

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