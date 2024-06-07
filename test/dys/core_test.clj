(ns dys.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [dys.core :as c]))

(defn state-after-single-roll
  [bet-type balance amount roll]
  (-> c/init-state
      (update :balance + balance)
      (c/place-bet bet-type amount)
      (c/do-roll roll)))

(deftest craps-test
  (testing "A 7 on the come out is a win"
    (is (= {:result :win} (c/evaluate-rolls [7]))))
  (testing "An 11 on the come out is a win"
    (is (= {:result :win} (c/evaluate-rolls [11]))))
  (testing "A 2 on the come out is a loss"
    (is (= {:result :loss} (c/evaluate-rolls [2]))))
  (testing "A 3 on the come out is a loss"
    (is (= {:result :loss} (c/evaluate-rolls [3]))))
  (testing "A 12 on the come out is a loss"
    (is (= {:result :loss} (c/evaluate-rolls [12]))))
  (testing "A 6 on the come out establishes a point"
    (is (= {:point 6} (c/evaluate-rolls [6]))))
  (testing "A 5 followed by a 5 is a win"
    (is (= {:result :win} (c/evaluate-rolls [5 5]))))
  (testing "A 4 followed by a 7 is a loss"
    (is (= {:result :loss} (c/evaluate-rolls [4 7]))))
  (testing "A 4 followed by a 12 is not a loss"
    (is (= {:point 4} (c/evaluate-rolls [4 12]))))
  (testing "A 9 followed by an 11 is not a win"
    (is (= {:point 9} (c/evaluate-rolls [9 11]))))
  (testing "A long roll without a result"
    (is (= {:point 5} (c/evaluate-rolls [5 2 3 6 12 8 11 8]))))
  (testing "A long losing roll"
    (is (= {:result :loss} (c/evaluate-rolls [5 2 3 6 12 8 11 8 7]))))
  (testing "A long winning roll"
    (is (= {:result :win} (c/evaluate-rolls [5 2 3 6 12 8 11 8 5]))))
  (testing "Before the come out roll, a pass bet is available"
    (is (some #{:pass} (c/available-bets c/init-state))))
  (testing "In the initial state, there are no bets"
    (is (zero? (count (:bets c/init-state)))))
  (testing "In the initial state there are no wins"
    (is (zero? (:wins c/init-state))))
  (testing "In the initial state there are no losses"
    (is (zero? (:losses c/init-state))))
  (testing "A player with a positive balance can place a pass bet"
    (let [state (-> c/init-state
                    (update :balance + 100)
                    (c/place-bet :pass 10))]
      (is (= 1 (count (:bets state))))))
  (testing "A player balance goes down when a bet is placed"
    (let [state (-> c/init-state
                    (update :balance + 100)
                    (c/place-bet :pass 10))]
      (is (= 90 (:balance state)))))
  (testing
    "If there are no bets, the number wins does not change after the come out roll"
    (is (zero? (:wins (c/do-roll c/init-state [5 2])))))
  (testing
    "If there are no bets, the number losses does not change after the come out roll"
    (is (zero? (:losses (c/do-roll c/init-state [5 2])))))
  (testing "A pass bet wins on the come out roll if a 7 is rolled"
    (is (= 1 (:wins (state-after-single-roll :pass 100 10 [5 2])))))
  (testing "A pass bet wins on the come out roll if an 11 is rolled"
    (is (= 1 (:wins (state-after-single-roll :pass 100 10 [5 6])))))
  (testing
    "If a pass bet wins, the players balance is increased by 2 * bet-amount"
    (is (= 110 (:balance (state-after-single-roll :pass 100 10 [5 2])))
        "bet size 10")
    (is (= 120 (:balance (state-after-single-roll :pass 100 20 [5 2])))
        "bet size 20"))
  (testing "If a pass bet wins, the number of losses is not increased"
    (is (zero? (:losses (state-after-single-roll :pass 100 10 [5 2])))))
  (testing "A pass bet loses on the come out roll if a 2 is rolled"
    (is (= 1 (:losses (state-after-single-roll :pass 100 10 [1 1])))))
  #_(testing "The player initially has a balance of 0"
      (is (= 0 (:balance c/init-state))))
  #_(testing "When a player bet wins, the balance increases"
      (is (= 110
             (:balance (c/play-pass-line (update c/init-state :balance + 100)
                                         10
                                         [7])))))
  #_(testing "When a player bet loses, the balance decreases"
      (is (= 90
             (:balance (c/play-pass-line (update c/init-state :balance + 100)
                                         10
                                         [12]))))))

(comment
  "[ ] Add convienence funtion to update balance
   [ ] The point is nil before the come-out roll")
