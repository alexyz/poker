package pet.ui.rep;

import java.util.*;

import pet.eq.*;
import pet.hp.*;

class HandStateUtil {
	/**
	 * convert hand into list of hand states
	 */
	public static List<HandState> getStates(Hand hand) {
		List<HandState> states = new Vector<HandState>();
		//TreeMap<Integer,String[]> holes = new TreeMap<Integer,String[]>();

		// initial state (not displayed)
		HandState hs = new HandState(hand.max);
		hs.pot = hand.db;
		hs.button = hand.button - 1;
		hs.actionSeat = -1;
		for (Seat seat : hand.seats) {
			SeatState ss = new SeatState(seat);
			if (seat.hole != null) {
				String[] hole = seat.hole.clone();
				Arrays.sort(hole, Cmp.revCardCmp);
				ss.hole = seat.hole;
			}
			ss.stack = seat.chips;
			hs.seats[seat.num - 1] = ss;
		}
		
		Poker poker = PokerUtil.getPoker(hand.gametype);
		List<String[]> holes = new ArrayList<String[]>();
		List<SeatState> holeSeats = new ArrayList<SeatState>();

		for (int s = 0; s < hand.streets.length; s++) {
			// clear bets, place card
			// TODO calc equity
			hs = hs.clone();
			String[] board = HandUtil.getStreetBoard(hand, s);
			hs.board = board;
			hs.note = HandUtil.getStreetName(hand.gametype, s);
			hs.action = null;
			hs.actionSeat = -1;
			holes.clear();
			holeSeats.clear();
			for (SeatState ss : hs.seats) {
				if (ss != null) {
					hs.pot += ss.bet;
					ss.bet = 0;
					ss.eq = null;
					if (ss.hole != null) {
						String[] hole = HandUtil.getStreetHole(hand, ss.seat, s);
						ss.hole = hole;
						holes.add(ss.hole);
						holeSeats.add(ss);
					}
				}
			}
			
			String[][] holesArr = holes.toArray(new String[holes.size()][]);
			HandEq[] eqs = poker.equity(hs.board, holesArr);
			for (int n = 0; n < holeSeats.size(); n++) {
				SeatState ss = holeSeats.get(n);
				ss.eq = eqs[n];
			}
			
			states.add(hs);

			// player actions for street
			for (Action act : hand.streets[s]) {
				hs = hs.clone();
				hs.action = act.act;
				if (act.amount > 0) {
					hs.action += " " + act.amount;
				}
				if (act.seat.discards > 0) {
					hs.action += " " + act.seat.discards;
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
		hs.board = hand.board; // prob not needed
		hs.note = "End";
		hs.actionSeat = -1;
		//hs.pot = hand.pot;
		hs.pot = 0;
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