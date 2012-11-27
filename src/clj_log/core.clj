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

(defn- make-message [ns message level & [pattern]]
  (let [msg {:ns (name (ns-name ns))
             :time (new Date)
             :message message
             :level level}]
    (if pattern (assoc msg :pattern pattern) msg)))

(defn- log-message [ns level message]
  (logging/log ns level nil 
               (let [wrt (new java.io.StringWriter)]
                 (clojure.pprint/pprint message wrt)
                 (.toString wrt))))

(defn gen-log [ns level message ex]
  (let [message-info   (make-message ns message level)         
         exception-info (ex-log ex)]
    (log-message ns level (merge message-info exception-info))))

(defmacro log
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
  [level message & [more]]  
  `(gen-log *ns* ~level ~message ~more))


(defn gen-logf [ns level pattern & args]
  (let [[format-args ex] 
        (if (instance? Throwable (last args)) [(butlast args) (last args)] [args nil])]    
    (log-message
      ns
      level
      (merge 
        (make-message ns (apply (partial format pattern) args) level pattern)
        (ex-log ex)))))

(defmacro logf
  "level can be :trace, :debug, :info, :warn, :error, :fatal
   pattern - string
   args - parameters which will be passed to the pattern to be filled in, can optionally end with a Throwable"
  [level pattern & args]
  `(gen-logf *ns* ~level ~pattern ~@args))

(defn read-log
  "accepts file name as input, a filter function which each item in the log will be checked against and maximum number of logs to retain"
  ([file-name] (read-log file-name nil nil))
  ([file-name param] (apply (partial read-log file-name) (if (number? param) [nil param] [param nil])))
  ([file-name log-filter max-size]
    (when (not (.exists (new java.io.File file-name)))
      (throw (new Exception (str "log " file-name " is not available!"))))
    (with-open [r (->> file-name
                    (new FileInputStream)
                    (new InputStreamReader)
                    (new PushbackReader))]
      (binding [*read-eval* false]
        (loop [logs '()]
          (let [item (try (read r nil nil) (catch Exception ex {:parse-error (.getMessage ex)}))] 
            (if item
              ;;if log reached max size, then drop items before adding new items
              ;;return tail end of the log up to max size
              (recur (if (or (nil? log-filter ) (log-filter item))
                       (conj 
                         (if (or (nil? max-size) (< (count logs) max-size))
                           logs (butlast logs)) item) 
                       logs))
              logs)))))))

