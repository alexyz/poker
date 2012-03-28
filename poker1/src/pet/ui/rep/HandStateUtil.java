package pet.ui.rep;

import java.util.*;

import pet.eq.Poker;
import pet.hp.*;

class HandStateUtil {
	/**
	 * convert hand into list of hand states
	 */
	public static List<HandState> getStates(Hand hand) {
		List<HandState> states = new Vector<HandState>();

		// initial state
		HandState hs = new HandState(hand.max);
		hs.pot = hand.db;
		hs.button = hand.button - 1;
		hs.actionSeat = -1;
		for (Seat seat : hand.seats) {
			SeatState ss = new SeatState();
			ss.name = seat.name;
			if (seat.hole != null) {
				String[] hole = seat.hole.clone();
				Arrays.sort(hole, Poker.revCardCmp);
				ss.hole = Poker.toString(seat.hole);
			}
			ss.stack = seat.chips;
			hs.seats[seat.num - 1] = ss;
		}

		//states.add(hs);

		for (int s = 0; s < hand.streets.length; s++) {
			// clear bets, place card
			hs = hs.clone();
			hs.board = Poker.toString(HandUtil.getStreetBoard(hand.board, s));
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
				hs.action = act.act;
				if (act.amount > 0) {
					hs.action += " " + act.amount;
				}
				hs.actionSeat = act.seat.num - 1;

				SeatState ss = hs.seats[act.seat.num - 1];
				if (act.act.equals("folds")) {
					ss.folded = true;
				} else if (act.amount > 0) {
					ss.bet += act.amount;
					ss.stack -= act.amount;
				}
				states.add(hs);
			}
		}
		
		// FIXME collect, return uncalled, showdown
		hs = hs.clone();
		hs.board = Poker.toString(hand.board);
		hs.note = "End";
		hs.actionSeat = -1;
		hs.pot = hand.pot;
		for (int s = 0; s < hand.seats.length; s++) {
			Seat seat = hand.seats[s];
			SeatState ss = hs.seats[seat.num - 1];
			ss.bet = seat.won;
			ss.won = seat.won > 0;
		}
		states.add(hs);
		

		return states;
	}

}