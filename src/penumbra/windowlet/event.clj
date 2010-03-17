;;   Copyright (c) Zachary Tellman. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;   which can be found in the file epl-v10.html at the root of this distribution.
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any other, from this software.

(ns penumbra.windowlet.event)

(defvar *unsubscribe* nil
  "Hook for unsubscribing.")

(defn create []
  (atom {}))

(defn subscribe! [event hook f]
  (swap! event #(update-in % [hook] (fn [x] (if x (conj x f) #{f})))))

(defn unique-subscribe! [event hook f]
  (swap! event #(assoc % hook #{f})))

(defn unsubscribe!
  [event hook f]
  (swap! event #(update-in % [hook] (fn [x] (disj x f)))))

(defn publish! [event hook & args]
  (let [wrapper-fn (fn [f]
                     (fn [& args]
                       (binding [*unsubscribe* (atom nil)]
                         (apply f args)
                         @*unsubscribe*)))]
    (doseq [f (->> @event hook (map wrapper-fn) (map #(apply % args)) (remove nil?))]
      (unsubscribe! event hook f))))
