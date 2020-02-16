(ns this-one.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.interceptor.helpers :refer [definterceptor defhandler]]
            [clj-http.client :as client]
            [ring.util.response :as ring-resp]
            [clojure.data.json :as json]
            [clojure.data.xml :as xml]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.json]))



(defn git-search [q]
  (let [ret (client/get
              (format "https://api.github.com/search/repositories?q=%s+language:clojure" q) {:accept :json})]
    (json/read-str (ret :body))))

(defn git-get
  [request]
  (http/json-response (git-search (get-in request [:query-params :q]))))



(defn about-page
  [request]
  (ring-resp/response
    (format "Clojure %s - served from %s"
            (clojure-version)
            (route/url-for ::about-page))))

(def mongo-uri "mongodb://aashrey:admin123@127.0.0.1:27017/sample")

(defn home-page
  [request]
  (prn request) (http/json-response "hi"))

(defn db-get-project [proj-name]
  (let [connect-string mongo-uri
        {:keys [conn db]} (mg/connect-via-uri connect-string)]
    (mc/find-maps db "catalog" {:proj-name proj-name})))

(defn get-project
  [request]
  (http/json-response
    (db-get-project (get-in request [:path-params :proj-name]))))

(defn get-projects
  [request]
  (let [uri mongo-uri {:keys [conn db]}
        (mg/connect-via-uri uri)]
    (http/json-response (mc/find-maps db "catalog"))))

(defn create-project [request]
  (prn (:json-params request))
  (let [incoming (:json-params request)
        connect-string mongo-uri {:keys [conn db]} (mg/connect-via-uri connect-string)]
    (ring-resp/created "the url" (mc/insert-and-return db "catalog" incoming))))


(defhandler token-check [request]
    (let [token (get-in request [:headers "x-catalog-token"])]
      (if (not (= token "o brave new world"))
        (assoc (ring-resp/response {:body "access denied"}) :status 403))))



;; Defines "/" and "/about" routes with their associated :get handlers.
;; The interceptors defined after the verb map (e.g., {:get home-page}
;; apply to / and its children (/about).
(def common-interceptors [(body-params/body-params) http/html-body token-check])

;; Tabular routes
(def routes #{["/" :get (conj common-interceptors `home-page)]
              ["/about" :get (conj common-interceptors `about-page)]
              ["/projects" :get (conj common-interceptors `get-projects)]
              ["/project/:proj-name" :get (conj common-interceptors `get-project)]
              ["/project" :post (conj common-interceptors `create-project)]
              ["/see-also" :get (conj common-interceptors `git-get)]})




;; Map-based routes
;(def routes `{"/" {:interceptors [(body-params/body-params) http/html-body]
;                   :get home-page
;                   "/about" {:get about-page}}})

;; Terse/Vector-based routes
;(def routes
;  `[[["/" {:get home-page}
;      ^:interceptors [(body-params/body-params) http/html-body]
;      ["/about" {:get about-page}]]]])


;; Consumed by this-one.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]

              ;; Tune the Secure Headers
              ;; and specifically the Content Security Policy appropriate to your service/application
              ;; For more information, see: https://content-security-policy.com/
              ;;   See also: https://github.com/pedestal/pedestal/issues/499
              ;;::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
              ;;                                                          :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
              ;;                                                          :frame-ancestors "'none'"}}

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ;;  This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
              ::http/type :jetty
              ;;::http/host "localhost"
              ::http/port (Integer. (or (System/getenv "PORT") 8080))
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false}})
                                        ;; Alternatively, You can specify you're own Jetty HTTPConfiguration
                                        ;; via the `:io.pedestal.http.jetty/http-configuration` container option.
                                        ;:io.pedestal.http.jetty/http-configuration (org.eclipse.jetty.server.HttpConfiguration.)

