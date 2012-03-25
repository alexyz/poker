package pet.ui.rep;

import java.util.List;
import java.util.Vector;

import pet.eq.Poker;
import pet.hp.Action;
import pet.hp.Hand;
import pet.hp.HandUtil;
import pet.hp.Seat;

class HandStateUtil {
	/**
	 * convert hand into list of hand states
	 */
	public static List<HandState> getStates(Hand hand) {
		List<HandState> states = new Vector<HandState>();

		// initial state
		HandState hs = new HandState(hand.max);
		hs.button = hand.button - 1;
		hs.actionSeat = -1;
		for (Seat seat : hand.seats) {
			SeatState ss = new SeatState();
			ss.name = seat.name;
			ss.cards = Poker.getCardString(seat.hand, true);
			ss.stack = seat.chips;
			hs.seats[seat.num - 1] = ss;
		}

		//states.add(hs);

		for (int s = 0; s < hand.streets.length; s++) {
			// clear bets, place card
			hs = hs.clone();
			hs.board = Poker.getCardString(HandUtil.getStreetBoard(hand.board, s), false);
			hs.note = HandUtil.getStreetName(hand.gametype, s);
			hs.action = null;
			hs.actionSeat = -1;
			for (SeatState ss : hs.seats) {
				if (ss != null) {
					hs.pot += ss.bet;
					ss.bet = 0;
				}
			}
			states.add(hs);

			// player actions
			for (Action act : hand.streets[s]) {
				hs = hs.clone();
				hs.action = act.act + " " + act.amount;
				hs.actionSeat = act.seat.num - 1;

				SeatState ss = hs.seats[act.seat.num - 1];
				if (act.act.equals("folds")) {
					ss.folded = true;
				} else if (act.amount > 0) {
					ss.bet += act.amount;
				}
				states.add(hs);
			}
		}
		
		// collect, return uncalled, showdown

		return states;
	}

}