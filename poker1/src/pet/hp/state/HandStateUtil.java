package pet.hp.state;

import java.util.*;

import pet.eq.*;
import pet.hp.*;

public class HandStateUtil {
	/**
	 * convert hand into list of hand states
	 */
	public static List<HandState> getStates(Hand hand) {
		List<HandState> states = new Vector<HandState>();

		// initial state (not displayed)
		HandState hs = new HandState(hand);
		hs.pot = hand.db;
		hs.button = hand.button - 1;
		hs.actionSeat = -1;
		for (Seat seat : hand.seats) {
			SeatState ss = new SeatState(seat);
			if (seat.hole != null) {
				String[] hole = seat.hole.clone();
				Arrays.sort(hole, Cmp.revCardCmp);
				ss.hole = hole;
			}
			ss.stack = seat.chips;
			hs.seats[seat.num - 1] = ss;
		}
		
		// equity stuff
		Poker poker = GameUtil.getPoker(hand.game.type);
		List<String[]> holes = new ArrayList<String[]>();
		List<SeatState> holeSeats = new ArrayList<SeatState>();
		Set<String> blockers = new TreeSet<String>();

		// for each street
		for (int s = 0; s < hand.streets.length; s++) {
			// clear bets, place card
			hs = hs.clone();
			String[] board = HandUtil.getStreetBoard(hand, s);
			hs.board = board;
			hs.note = GameUtil.getStreetName(hand.game.type, s);
			hs.action = null;
			hs.actionSeat = -1;
			holes.clear();
			holeSeats.clear();
			blockers.clear();
			
			for (SeatState ss : hs.seats) {
				if (ss != null) {
					hs.pot += ss.amount;
					ss.amount = 0;
					ss.eq = null;
					ss.acts = 0;
					// get hole cards of live hands
					if (ss.hole != null && !ss.folded) {
						String[] hole = HandUtil.getStreetHole(hand, ss.seat, s);
						ss.hole = hole;
						// make sure hand has minimum number of cards, pass others as blockers
						if (hole.length > GameUtil.getMinHoleCards(hand.game.type)) {
							holes.add(ss.hole);
							holeSeats.add(ss);
						} else {
							blockers.addAll(Arrays.asList(hole));
						}
					}
				}
			}
			for (SeatState ss : hs.seats) {
				if (ss != null) {
					// need to know pot to calc spr
					ss.spr = !ss.folded && hs.pot != 0 ? (ss.stack*1f) / hs.pot : 0;
				}
			}
			
			String[][] holesArr = holes.toArray(new String[holes.size()][]);
			String[] blockersArr = blockers.toArray(new String[blockers.size()]);
			HandEq[] eqs = poker.equity(hs.board, holesArr, blockersArr);
			for (int n = 0; n < holeSeats.size(); n++) {
				SeatState ss = holeSeats.get(n);
				ss.eq = eqs[n];
			}
			
			states.add(hs);
			int trail = 0;
			int lastbet = 0;

			// player actions for street
			for (Action act : hand.streets[s]) {
				hs = hs.clone();
				hs.action = act;
				hs.actionSeat = act.seat.num - 1;

				SeatState ss = hs.seats[act.seat.num - 1];
				ss.bpr = 0;
				ss.acts++;
				
				if (act.type == Action.FOLD_TYPE) {
					ss.folded = true;
					
				} else if (act.amount > 0) {
					ss.amount += act.amount;
					if (act.type == Action.BET_TYPE || act.type == Action.RAISE_TYPE) {
						int potsz = (hs.pot + trail + 2 * lastbet);
						if (potsz > 0) {
							ss.bpr = (ss.amount * 100f) / potsz;
						}
					}
					//System.out.println("act " + act);
					//System.out.println("  pot=" + hs.pot + " trail=" + trail + " (2)lastbet=" + (2*lastbet) + " potsz=" + potsz + " bet=" + ss.amount + " bpr=" + ss.bpr);
					
					ss.stack -= act.amount;
					lastbet = Math.max(lastbet, act.amount);
					trail += act.amount;
				}
				
				
				
				states.add(hs);
			}
		}
		
		// collect, return uncalled, showdown
		hs = hs.clone();
		hs.board = hand.board; // prob not needed
		hs.note = "End";
		hs.actionSeat = -1;
		//hs.pot = hand.pot;
		hs.pot = 0;
		for (int s = 0; s < hand.seats.length; s++) {
			Seat seat = hand.seats[s];
			SeatState ss = hs.seats[seat.num - 1];
			ss.amount = seat.won;
			ss.won = seat.won > 0;
			ss.spr = 0;
			ss.bpr = 0;
		}
		states.add(hs);
		
		return states;
	}

}