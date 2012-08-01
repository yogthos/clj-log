(ns clj-log.test.core
  (:use clojure.test clj-log.core))

(def log-file "test.log")

(deftest test-log
(.write (clojure.java.io/writer log-file) "")  
  (log :info "foo")
  (let [log-item (first (read-log log-file))
        {:keys [ns time message level]} log-item]    
    (is (and (instance? java.util.Date time)
             (not-empty  ns)
             (= "foo" message)
             (= :info level)))))


(deftest test-logf
  (.write (clojure.java.io/writer log-file) "")
  (logf :info "%s accidentally the whole %s" "I" ".jar file")
  (let [log-item (first (read-log log-file))
        {:keys [message pattern]} log-item]    
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
  (log :info "bar")
  (log :warn "baz")  
  (log :info "last item")
  (= 1 (count (read-log log-file #(= :error (:level %)))))
  (= ["last item" "baz"] (map :message (read-log log-file 2)))
  (= ["error" "foo" "last item"] (map :message (read-log log-file #(not= "baz" (:message %)) 3))))
