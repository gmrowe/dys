(ns dys.core)

(defn win?
  [state roll]
  (if (some? state) (= (:point state) roll) (#{7 11} roll)))

(defn loss? [state roll] (if (some? state) (= 7 roll) (#{2 3 12} roll)))

(defn evaluate-roll
  [state roll]
  (cond (win? state roll) {:result :win}
        (loss? state roll) {:result :loss}
        (some? state) state
        :else {:point roll}))

(defn evaluate-rolls [rs] (reduce evaluate-roll nil rs))

(def init-state {:balance 0, :wins 0, :losses 0})

(defn play-pass-line
  [player bet rolls]
  (let [evaluation (evaluate-rolls rolls)]
    (cond (= :win (:result evaluation)) (update player :balance + bet)
          (= :loss (:result evaluation)) (update player :balance - bet))))

(defn available-bets [state] #{:pass})

(defn place-bet
  [state bet-type amount]
  (-> state
      (update :bets (fnil conj []) {:bet-type bet-type, :amount amount})
      (update :balance - amount)))

(defn do-roll
  [state [d1 d2]]
  (if (seq (:bets state))
    (cond (#{7 11} (+ d1 d2)) (-> state
                                  (update :wins inc)
                                  (update :balance
                                          +
                                          (* 2
                                             (-> state
                                                 :bets
                                                 first
                                                 :amount))))
          (#{2} (+ d1 d2)) (assoc state :losses 1))
    state))

(comment
  ("
General algorithm:
  1. Get initial bank size from player
  2. Ask player if they would like to quit
  3. if player wants to quit, print a summary and exit otherwise continue
  4. Present player with a list of currently legal bets
  5. Allow player to choose their bet(s)
  6. Roll the dice
  7. Get results from all bets
  8. Update user's bank with any winnings
  9. Present user with bet result summaryn
  10. Goto step 2

  * [stretch]
    Allow player other configurations such as
       i. table minimum/maximum
       ii. max-odds on pass/come bets (and their `dont` equivalents)
  * [stretch]
    Allow player to script betting systems and run monte carlo
    simulations
  * [stretch]
    Create GUI with animations"))
