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
		Poker poker = GameUtil.getPoker(hand.game);
		List<String[]> holes = new ArrayList<String[]>();
		List<SeatState> holeSeats = new ArrayList<SeatState>();
		Set<String> blockers = new TreeSet<String>();

		// for each street
		for (int s = 0; s < hand.streets.length; s++) {
			// clear bets, place card
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
					ss.meq = null;
					ss.acts = 0;
					// get hole cards of live hands
					if (ss.hole != null && !ss.folded) {
						String[] hole = HandUtil.getStreetHole(hand, ss.seat, s);
						Arrays.sort(hole, Cmp.revCardCmp);
						ss.hole = hole;
						// make sure hand has minimum number of cards, pass others as blockers
						if (hole.length > GameUtil.getMinHoleCards(hand.game.type)) {
							holes.add(hole);
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
			synchronized (poker) {
				MEquity[] eqs = poker.equity(hs.board, holesArr, blockersArr);
				for (int n = 0; n < holeSeats.size(); n++) {
					SeatState ss = holeSeats.get(n);
					ss.meq = eqs[n];
				}
			}
			
			states.add(hs.clone());
			int trail = 0;
			int lastbet = 0;
			//int p = hs.pot;

			// player actions for street
			for (Action act : hand.streets[s]) {
				System.out.println("act " + act);
				hs = hs.clone();
				hs.action = act;
				hs.actionSeat = act.seat.num - 1;

				SeatState ss = hs.seats[act.seat.num - 1];
				ss.bpr = 0;
				ss.acts++;
				
				if (act.type == Action.FOLD_TYPE) {
					ss.folded = true;
					
				} else if (act.amount != 0) {
					// pot raise amount
					int pr = hs.pot + trail + 2 * (lastbet - ss.amount);
					//System.out.println("  p=" + hs.pot + " t=" + trail + " l=" + lastbet + " sa=" + ss.amount + " => pr=" + pr);
					
					ss.amount += act.amount;
					
					if (act.type == Action.BET_TYPE || act.type == Action.RAISE_TYPE) {
						if (pr > 0) {
							ss.bpr = (act.amount * 100f) / pr;
							//System.out.println("  aa=" + act.amount + " bpr=" + ss.bpr);
						}
						
					} else if (act.type == Action.COLLECT_TYPE) {
						ss.won = true;
						ss.amount = -act.amount;
					}
					
					lastbet = Math.max(lastbet, ss.amount);
					ss.stack -= act.amount;
					trail += act.amount;
				}
				
				states.add(hs.clone());
			}
		}
		
		return states;
	}

}