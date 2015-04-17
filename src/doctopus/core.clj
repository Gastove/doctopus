(ns doctopus.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


;; Gavin and I wrote all this as a kind of notes; not actually convinced we
;; need it any more? Or at least: right now, all this does is kill compilation.
;;
;; (defn parse-a-doc
;;   [some-local-path some-function some-output-path]
;;   (some-function some-local-path some-output-path))
;;
;; (defn parse-markdown
;;   [args]
;;   (some-clojure-markdown-lib args))
;;
;; (defn parse-rst
;;   [args]
;;   (sh/exec-command "make html" args))
;;
;; (def a-configuration {:uri "/home/gastove/foo/my-hella-sweet-repo"
;;                       :output-path "/who/knows"
;;                       :documentation-type :markdown})
;;
;; (defmulti parse-documentation :documentation-type)
;;
;; (defmethod parse-documentation :markdown
;;   [type & args])
