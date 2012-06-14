clj-log
=======

## structural logging for Clojure

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




