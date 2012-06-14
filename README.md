clj-log
=======

structural logging for Clojure using log4j, outputs logs using Clojure data structures

## Installation

Leiningen

```clojure
[clj-log "0.0.1"]
```

## Usage

A logger must be initialized before use, this is done with `init-logger`, after that `log` function can be used to do logging.

```clojure
(init-logger :test-logger :info [{:id :file-appender :type :file :filename "test.log" :append? false}])
(log :test-logger :info "foo")

```

output

```clojure
{:logger :test-logger,
 :time #inst "2012-06-14T02:05:40.487-00:00",
 :message "foo",
 :level :trace}
```

```clojure
(try 
  (throw (new Exception "foobar"))
  (catch Exception ex
    (log :test-logger :error "an error has occured" ex)))
```

```clojure
{:exception
 {:class java.lang.Exception,
  :cause nil,
  :message "foobar",
  :localized "foobar",
  :stack-trace
  [{:class "clj_log.test.core$eval1265",
    :file "NO_SOURCE_FILE",
    :line 2,
    :method "invoke"}
   {:class "clojure.lang.Compiler",
    :file "Compiler.java",
    :line 6511,
    :method "eval"}
   {:class "clojure.lang.Compiler",
    :file "Compiler.java",
    :line 6477,
    :method "eval"}
   {:class "clojure.core$eval",
    :file "core.clj",
    :line 2797,
    :method "invoke"}
   {:class "clojure.main$repl$read_eval_print__6405",
    :file "main.clj",
    :line 245,
    :method "invoke"}
   {:class "clojure.main$repl$fn__6410",
    :file "main.clj",
    :line 266,
    :method "invoke"}]},
 :logger :test-logger,
 :time #inst "2012-06-14T02:56:35.496-00:00",
 :message "an error has occured",
 :level :error}
```

When initializing a logger you must supply the id of the logger, it's log level and a vector of appenders associated with the logger. 

Following log levels are available:

* :debug
* :info
* :warn
* :error
* :trace
* :fatal

The following appenders are available:

### Console appender

* :type :console-appender

### File appender

additional properties:

* :type :file
* :filename string
* :append? boolean
* :buffered? boolean
* :buf-size int

### Rolling file appender

additional properties:

* :max-size long or a string formed as integer followed by suffix "KB", "MB" or "GB", eg: 10KB will be interpreted as 10240
* :max-backups number indicating maximum number of backups to keep

### Daily rolling file appender

additional properties:

* :date-pattern string [format detials](http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/DailyRollingFileAppender.html)


## Log viewing

`read-log` reads a log file into memory, accepts an optional filter function

```clojure
(read-log "test.log")
(read-log "test.log" (fn [item] (= #inst "2012-06-14T02:25:33.960-00:00" (:time item))))
``` 


