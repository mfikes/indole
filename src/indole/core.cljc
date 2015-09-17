(ns indole.core)

(defn- get-current-time-millis
  []
  #?(:clj  (System/currentTimeMillis)
     :cljs (. (js/Date.) (getTime))))

(defn make-rate-limiter
  "Creates a rate limiter with a specified bucket size, where bucket entries are replinished over a
  period specified in milliseconds."
  [period-ms bucket-size]
  {:pre [(pos? period-ms) (pos? bucket-size)]}
  (atom {:increment-period-ms   (quot period-ms bucket-size)
         :bucket-size           bucket-size
         :bucket-level          bucket-size
         :last-time-incremented (get-current-time-millis)}))

(defn- increment-bucket-level
  [current-level max-level increments]
  (min (+ current-level increments) max-level))

(defn- decrement-bucket-level
  [current-level]
  (max (dec current-level) 0))

(defn- mark-decremented
  [m]
  (assoc m :decremented? (pos? (:bucket-level m))))

(defn- charge-request
  [rate-limiter-state current-time-millis]
  (let [increment-period-ms (:increment-period-ms rate-limiter-state)
        adjusted-state (if (and (< (:bucket-level rate-limiter-state) (:bucket-size rate-limiter-state))
                             (> current-time-millis (:last-time-incremented rate-limiter-state)))
                         (let [bucket-increments (- (quot current-time-millis increment-period-ms)
                                                   (quot (:last-time-incremented rate-limiter-state) increment-period-ms))]
                           (if (pos? bucket-increments)
                             (-> rate-limiter-state
                               (update-in [:bucket-level]
                                 increment-bucket-level (:bucket-size rate-limiter-state) bucket-increments)
                               (assoc :last-time-incremented current-time-millis))
                             rate-limiter-state))
                         rate-limiter-state)]
    (-> adjusted-state
      (mark-decremented)
      (update-in [:bucket-level] decrement-bucket-level))))

(defn can-charge?!
  "Checks if the supplied rate limiter can be used, and if so, decrements its bucket level and returns true."
  [rate-limiter]
  (:decremented? (swap! rate-limiter charge-request (get-current-time-millis))))
