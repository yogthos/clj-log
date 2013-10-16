clj-log
=======

S-expression logger for Clojure using [clojure.tools.logging](http://clojure.github.com/tools.logging/), which outputs logs using Clojure data structures.

## Installation

Leiningen

```clojure
[clj-log "0.4.5"]
```

## Usage

To enable logging you must first setup your logging properties, eg:

    log4j.logger.clj-log.test.core=DEBUG, rollingFile 
    log4j.appender.rollingFile=org.apache.log4j.RollingFileAppender
    log4j.appender.rollingFile.File=test.log
    log4j.appender.rollingFile.MaxFileSize=100KB
    log4j.appender.rollingFile.MaxBackupIndex=2
    log4j.appender.rollingFile.layout = org.apache.log4j.PatternLayout
    log4j.appender.rollingFile.layout.ConversionPattern=%m%n


note: make sure that the logging pattern does not append anything to the log as it will produce garbage, see clojure.tools.logging for more documentation

Accepted logging levels are:

```Clojure
:trace, :debug, :info, :warn, :error, :fatal
```

`log` accepts level, message, and an optional Throwable as parameters

'logf` accepts level, pattern, args, and an optional Throwable as parameters 
### Examples

```clojure
(ns example
 (:use clj-log.core))

(log :info "foo")

;output

{:ns "example",
 :time #inst "2012-06-14T21:46:12.980-00:00",
 :message "foo",
 :level :info}
 
 
 ;message can be any clojure data structure
 (log :warn {:foo "bar"})
 
 ;output 
 
 {:ns "example",
 :time #inst "2012-06-15T02:55:17.392-00:00",
 :message {:foo "bar"},
 :level :warn}
 
```

```clojure
(logf :info "%s accidentally the whole %s" "I" ".jar file")

;output

{:pattern "%s accidentally the whole %s",
 :ns "example",
 :time #inst "2012-06-15T02:25:42.070-00:00",
 :message "I accidentally the whole .jar file",
 :level :info}
```

```clojure
(try 
  (throw (new Exception "foobar"))
  (catch Exception ex
    (log :error "an error has occured" ex)))

;truncated output

{:exception
 {:class java.lang.Exception,
  :cause
  {:exception
   {:class java.lang.Exception,
    :cause nil,
    :message "foobar",
    :localized "foobar",
    :stack-trace
    [{:class "clj_log.test.core$eval964",
      :file "NO_SOURCE_FILE",
      :line 1,
      :method "invoke"}
     {:class "clojure.lang.Compiler",
      :file "Compiler.java",
      :line 6511,
      :method "eval"}          
     {:class "java.util.concurrent.ThreadPoolExecutor$Worker",
      :file "ThreadPoolExecutor.java",
      :line 603,
      :method "run"}
     {:class "java.lang.Thread",
      :file "Thread.java",
      :line 722,
      :method "run"}]}},
  :message "caused by: ",
  :localized "caused by: ",
  :stack-trace
  [{:class "clj_log.test.core$eval964",
    :file "NO_SOURCE_FILE",
    :line 4,
    :method "invoke"}
   {:class "clojure.lang.Compiler",
    :file "Compiler.java",
    :line 6511,
    :method "eval"}      
   {:class "java.util.concurrent.ThreadPoolExecutor$Worker",
    :file "ThreadPoolExecutor.java",
    :line 603,
    :method "run"}
   {:class "java.lang.Thread",
    :file "Thread.java",
    :line 722,
    :method "run"}]},
 :ns "example",
 :time #inst "2012-06-14T21:51:56.871-00:00",
 :message "an error has occured",
 :level :error}
```

## Log viewing

`read-log` reads a log file into memory, accepts an optional filter function, and max number of logs to retain

```clojure
;read all logs in the file
(read-log "test.log")

;read last 5 logs
(read-log "test.log" 5)

;read logs with the matching timestamp
(read-log "test.log" (fn [item] (= #inst "2012-06-14T02:25:33.960-00:00" (:time item))))

;read last 15 logs with level :warn
(read-log "test.log" (fn [item] (= :warn (:level item))) 15)
``` 


