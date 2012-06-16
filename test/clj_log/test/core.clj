(ns clj-log.test.core
  (:use [clj-log.core])
  (:use [clojure.test]))

(def log-file "test.log")

(deftest test-log
(.write (clojure.java.io/writer log-file) "")  
  (log :info "foo")
  (let [{:keys [ns time message level]} (first (read-log log-file))]
    (is (and (instance? java.util.Date time)
             (= "clj-log.test.core" ns)
             (= "foo" message)
             (= :info level)))))


(deftest test-logf
  (.write (clojure.java.io/writer log-file) "")
  (logf :info "%s accidentally the whole %s" "I" ".jar file")
  (let [{:keys [message pattern]} (first (read-log log-file))]
    (is (and (= "I accidentally the whole .jar file" message)
             (= "%s accidentally the whole %s" pattern)))))


(deftest ex-log
  (.write (clojure.java.io/writer log-file) "")
  (try 
    (throw (new Exception "foobar"))
    (catch Exception ex
      (log :error "an error has occured" (new Exception "caused by: " ex))))
  
  (let [{:keys [exception]} (first (read-log log-file))]
    (= "foobar"
       (->> exception :cause :message))))


(deftest log-reading
  (.write (clojure.java.io/writer log-file) "")
  (log :info "foo")
  (log :error "error" (new Exception "oops"))
  (= 1 (count (read-log log-file (fn [x] (= :error (:level x)))))))


