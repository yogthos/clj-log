clj-log
=======

Structural logger for Clojure using [clojure.tools.logging](http://clojure.github.com/tools.logging/), which outputs logs using Clojure data structures.

## Installation

Leiningen

```clojure
[clj-log "0.2"]
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

### Examples

```clojure

(log :info "foo")

```

output:

```clojure
{:ns "clj-log.test.core",
 :time #inst "2012-06-14T21:46:12.980-00:00",
 :message "foo",
 :level :info}
```

```clojure
(try 
  (throw (new Exception "foobar"))
  (catch Exception ex
    (log :error "an error has occured" ex)))
```
truncated output:

```clojure
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
 :ns "clj-log.test.core",
 :time #inst "2012-06-14T21:51:56.871-00:00",
 :message "an error has occured",
 :level :error}
```

## Log viewing

`read-log` reads a log file into memory, accepts an optional filter function

```clojure
(read-log "test.log")
(read-log "test.log" (fn [item] (= #inst "2012-06-14T02:25:33.960-00:00" (:time item))))
``` 


