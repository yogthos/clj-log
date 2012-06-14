(ns clj-log.test.core
  (:use [clj-log.core])
  (:use [clojure.test]))

(deftest test-log
  (clojure.java.io/delete-file "test.log")
  (init-logger :test-logger :info [{:id :file-appender :type :file :filename "test.log" :append? false}])
  (log :test-logger :info "foo")
  (let [{:keys [logger time message level]} (first (read-log "test.log"))]
    (is (and (instance? java.util.Date time)
             (= :test-logger logger)
             (= "foo" message)
             (= :info level)))))


