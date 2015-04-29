(ns doctopus.search)

(def elastic-search-configs
  {:settings
   {:index
    {"analysis"
     {"analyzer"
      {"html_stopwords" {"type" "standard" ;; Cannot find any record of what else might go here
                         "filter" ["html_strip" "stop"]} ;; Removes English stop words and HTML characters
       }}}}
   :mappings {"document" {:properties {:body {:type     "string"
                                              :analyzer "html_stopwords"}}}}}) ;; Hrm.
