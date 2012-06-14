(ns clj-log.core
  (:use clojure.pprint)
  (:import
    java.util.Date
    java.io.StringWriter
    [org.apache.log4j Level Logger ConsoleAppender PatternLayout DailyRollingFileAppender FileAppender]
    [java.io File PushbackReader InputStreamReader FileInputStream]))


(defn- logger [& [id]]
  (doto (if id (Logger/getLogger (name id)) (Logger/getRootLogger))
    (.addAppender
      (new ConsoleAppender (new PatternLayout PatternLayout/DEFAULT_CONVERSION_PATTERN )
           ConsoleAppender/SYSTEM_OUT))))


(def ^{:private true} logger-memo (memoize logger))


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


(defn init-logger [logger-name level appenders]
  (let [layout (new PatternLayout PatternLayout/DEFAULT_CONVERSION_PATTERN )
        logger (logger-memo logger-name)]
    (.setLevel logger
      (condp = level
        :all   Level/ALL
        :debug Level/DEBUG
        :error Level/ERROR
        :fatal Level/FATAL
        :info  Level/INFO
        :trace Level/TRACE
        :warn  Level/WARN))
    (.removeAllAppenders logger)
    
    (doseq [{:keys [id type filename append? buffered? buf-size date-pattern]} appenders]
      (when (nil? id) (throw (new Exception "id is required for an appender")))
      (.removeAppender logger (name id))
      (.addAppender logger
        (doto
          (cond
            (= :console type)
            (new ConsoleAppender layout ConsoleAppender/SYSTEM_OUT)
            
            (or (= :file type) (= :rolling-file type))
            (let [appender (if (= :file type)
                             (new FileAppender layout filename)
                             (new DailyRollingFileAppender layout filename date-pattern))]
              (when append? (.setAppend appender true))
              (when buffered? (.setBufferedIO appender true))
              (when buf-size (.setBufferSize appender (int buf-size)))
              appender))
          (.setName (name id)))))))


(defn log [logger-name level message & [ex]]
  (let [logger (logger-memo logger-name)
        message-info {:logger logger-name
                      :time (new Date)
                      :message message
                      :level level}
        exception-info (ex-log ex)
        log-message (with-out-str
                      (pprint (merge message-info exception-info)))]
    (condp = level
      :debug (.debug logger log-message)
      :info  (.info logger log-message)
      :warn  (.warn logger log-message)
      :error (.error logger log-message)
      :trace (.trace logger log-message)
      :fatal (.fatal logger log-message))))


(defn read-log [file-name]
  (with-open [r (->> file-name
                  (new FileInputStream)
                  (new InputStreamReader)
                  (new PushbackReader))]
    (binding [*read-eval* false]
      (loop [logs []]
        (if-let [item (read r nil nil)]
          (recur (conj logs item))
          logs)))))
