(ns clj-log.core
  (:require [clojure.tools.logging :as logging])
  (:import java.util.Date
           [java.io File PushbackReader InputStreamReader FileInputStream]))


(defn- stack-trace [ex]
  (mapv (fn [trace]
          {:class (.getClassName trace)
           :file (.getFileName trace)
           :line (.getLineNumber trace)
           :method (.getMethodName trace)})
        (.getStackTrace ex)))


(defn- ex-log [ex]
  (when ex
    {:exception
     {:class (class ex)
      :cause (ex-log (.getCause ex))
      :message (.getMessage ex)
      :localized (.getLocalizedMessage ex)
      :stack-trace (stack-trace ex)}}))

(defn- make-message [message level & [pattern]]
  (let [msg {:ns (name (ns-name *ns*))
             :time (new Date)
             :message message
             :level level}]
    (if pattern (assoc msg :pattern pattern) msg)))

(defn- log-message [level message]
  (logging/log *ns* level nil (with-out-str (clojure.pprint/pprint message))))

(defn log
  "level can be :trace, :debug, :info, :warn, :error, :fatal
   message - string
   ex - Throwable
  
   logs the message in format 
   {:ns namespace
    :time log-time
    :message message
    :level level
     ;optional exception log
     :exception {
      :class exception class
      :cause throwable
      :message message
      :localized localized message
      :stack-trace [{:class class name
                     :file file name
                     :line number
                     :method}}"
  [level message & [ex]]
  (println (ns-name *ns*))
  (let [message-info   (make-message message level)         
        exception-info (ex-log ex)]
    (log-message level (merge message-info exception-info))))


(defn logf [level pattern & args]
  (let [[format-args ex] 
        (if (instance? Throwable (last args)) [(butlast args) (last args)] [args nil])]
    (merge 
                     (make-message (apply (partial format pattern) args) level pattern)
                     (ex-log ex))
    (log-message
      level
      (merge 
        (make-message (apply (partial format pattern) args) level pattern)
        (ex-log ex)))))


(defn read-log
  "accepts file name as input and a filter function which each item in the log will be checked against"
  [file-name & [log-filter]]
  (with-open [r (->> file-name
                  (new FileInputStream)
                  (new InputStreamReader)
                  (new PushbackReader))]
    (binding [*read-eval* false]
      (loop [logs []]
        (if-let [item (read r nil nil)]
          (recur (if (or (nil? log-filter ) (log-filter item))
                   (conj logs item) logs))
          logs)))))

