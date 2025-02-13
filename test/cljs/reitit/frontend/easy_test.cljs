(ns reitit.frontend.easy-test
  (:require [clojure.test :refer [deftest testing is are async]]
            [reitit.core :as r]
            [reitit.frontend.easy :as rfe]
            [reitit.frontend.history :as rfh]
            [goog.events :as gevents]))

(def browser (exists? js/window))

(def router (r/router ["/"
                       ["" ::frontpage]
                       ["foo" ::foo]
                       ["bar/:id" ::bar]]))

;; TODO: Only tests fragment history, also test HTML5?

(deftest easy-history-routing-test
  (when browser
    (gevents/removeAll js/window goog.events.EventType.POPSTATE)
    (gevents/removeAll js/window goog.events.EventType.HASHCHANGE)

    (async done
      (let [n (atom 0)]
        ;; This also validates that rfe/history is set during initial on-navigate call
        (rfe/start! router
                    (fn on-navigate [match history]
                      (let [url (rfh/-get-path history)]
                        (case (swap! n inc)
                          1 (do (is (some? (:popstate-listener history)))
                                (is (= "/" url)
                                    "start at root")
                                (rfe/push-state ::foo))
                          ;; 0. /
                          ;; 1. /foo
                          2 (do (is (= "/foo" url)
                                    "push-state")
                                (.back js/window.history))
                          ;; 0. /
                          3 (do (is (= "/" url)
                                    "go back")
                                (rfe/navigate ::bar {:path-params {:id 1}}))
                          ;; 0. /
                          ;; 1. /bar/1
                          4 (do (is (= "/bar/1" url)
                                    "push-state 2")
                                (rfe/replace-state ::bar {:id 2}))
                          ;; 0. /
                          ;; 1. /bar/2
                          5 (do (is (= "/bar/2" url)
                                    "replace-state")
                                (rfe/set-query {:a 1}))
                          ;; 0. /
                          ;; 1. /bar/2
                          ;; 2. /bar/2?a=1
                          6 (do (is (= "/bar/2?a=1" url)
                                    "update-query with map")
                                (rfe/set-query #(assoc % :b "foo") {:replace true}))
                          ;; 0. /
                          ;; 1. /bar/2
                          ;; 2. /bar/2?a=1&b=foo
                          7 (do (is (= "/bar/2?a=1&b=foo" url)
                                    "update-query with fn")
                                (.go js/window.history -2))
                          ;; 0. /
                          8 (do (is (= "/" url)
                                    "go back two events")

                                ;; Reset to ensure old event listeners aren't called
                                (rfe/start! router
                                            (fn on-navigate [match history]
                                              (let [url (rfh/-get-path history)]
                                                (case (swap! n inc)
                                                  9 (do (is (= "/" url)
                                                            "start at root")
                                                        (rfe/push-state ::foo))
                                                  10 (do (is (= "/foo" url)
                                                             "push-state")
                                                         (rfh/stop! @rfe/history)
                                                         (done))
                                                  (do
                                                    (is false (str "extra event 2" {:n @n, :url url}))
                                                    (done)))))
                                            {:use-fragment true}))
                          (do
                            (is false (str "extra event 1" {:n @n, :url url}))
                            (done)))))
                    {:use-fragment true})))))
