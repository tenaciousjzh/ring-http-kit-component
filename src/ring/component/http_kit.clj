(ns ring.component.http-kit
  (:require [schema.core :as schema]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :refer [run-server]]))

(defschema ServerOpts {(schema/optional-key :ip) schema/Str
                       (schema/optional-key :port) (schema/both schema/Int
                                                                (schema/pred #(<= 0 % 65535)))
                       (schema/optional-key :thread) (schema/both schema/Int
                                                                  (schema/pred pos?))
                       (schema/optional-key :worker-name-prefix) schema/Str
                       (schema/optional-key :queue-size) (schema/both schema/Int
                                                                      (schema/pred pos?))
                       (schema/optional-key :max-body) (schema/both schema/Int
                                                                    (schema/pred pos?))
                       (schema/optional-key :max-line) (schema/both schema/Int
                                                                    (schema/pred pos?))})

(defrecord HttpKitServer [app]
  component/Lifecycle
  (start [component]
    (if (:server component)
      component
      (let [options (-> component
                        (dissoc :app)
                        (assoc :join? false))
            handler (atom (delay (:handler app)))
            server (run-server (fn [req] (@@handler req)) options)]
        (assoc component
               :server server
               :handler handler))))
  (stop [component]
    (if-let [server (:server component)]
      (do
        (server)
        (dissoc component :server :handler))
      component)))

(defn http-kit-server [options]
  (map->HttpKitServer options))
