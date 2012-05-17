package pet.hp.state;

import java.util.*;

import pet.eq.*;
import pet.hp.*;

public class HandStateUtil {
	
	private static final ArrayList<List<HandState>> cache = new ArrayList<List<HandState>>();
	private static final int cacheSize = 10;
	
	/**
	 * Get the first seat state for each street for the given seat
	 */
	public static List<SeatState> getFirst(List<HandState> states, int seatNum) {
		ArrayList<SeatState> sss = new ArrayList<SeatState>();
		int si = -1;
		for (HandState hs : states) {
			if (hs.actionSeat != null && hs.actionSeat.seat.num == seatNum) {
				if (hs.streetIndex > si) {
					sss.add(hs.actionSeat);
					si = hs.streetIndex;
				}
			}
		}
		return sss;
	}
	
	/**
	 * convert hand into list of hand states
	 */
	public static synchronized List<HandState> getStates(Hand hand) {
		for (List<HandState> l : cache) {
			if (l.get(0).hand.id.equals(hand.id)) {
				return l;
			}
		}
		
		List<HandState> states = new Vector<HandState>();

		// initial state (not displayed)
		HandState hs = new HandState(hand);
		hs.pot = hand.antes;
		hs.button = hand.button - 1;
		hs.actionSeat = null;
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
			
			//
			// state for clear bets, place card
			//
			String[] board = HandUtil.getStreetBoard(hand, s);
			hs.board = board;
			hs.note = GameUtil.getStreetName(hand.game.type, s);
			hs.action = null;
			hs.streetIndex = s;
			hs.actionSeat = null;
			holes.clear();
			holeSeats.clear();
			blockers.clear();
			
			for (SeatState ss : hs.seats) {
				if (ss != null) {
					hs.pot += ss.amount;
					ss.amount = 0;
					ss.meq = null;
					ss.actionNum = 0;
					// get hole cards of live hands
					if (ss.hole != null && !ss.folded) {
						String[] hole = HandUtil.getStreetHole(hand, ss.seat, s);
						Arrays.sort(hole, Cmp.revCardCmp);
						ss.hole = hole;
						// make sure hand has minimum number of cards, pass others as blockers
						if (hole.length >= GameUtil.getMinHoleCards(hand.game.type)) {
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
			MEquity[] eqs = poker.equity(hs.board, holesArr, blockersArr);
			for (int n = 0; n < holeSeats.size(); n++) {
				SeatState ss = holeSeats.get(n);
				ss.meq = eqs[n];
			}
			
			states.add(hs.clone());
			
			//
			// states for player actions for street
			//
			
			int trail = 0;
			int lastbet = 0;
			for (Action act : hand.streets[s]) {
				System.out.println();
				System.out.println("act " + act);
				hs = hs.clone();
				hs.action = act;
				
				SeatState ss = hs.seats[act.seat.num - 1];
				hs.actionSeat = ss;
				ss.bpr = 0;
				ss.ev = 0;
				ss.actionNum++;
				
				if (act.type == Action.FOLD_TYPE) {
					ss.folded = true;
					ss.spr = 0;
					
				} else if (act.amount != 0) {
					// pot raise amount
					int potraise = hs.pot + trail + 2 * (lastbet - ss.amount);
					System.out.println("MAX: pot=" + hs.pot + " trail=" + trail + " lastbet=" + lastbet + " committed=" + ss.amount + " => potraise " + potraise);
					
					ss.amount += act.amount;
					int tocall = 0;
					
					switch (act.type) {
						case Action.BET_TYPE:
						case Action.RAISE_TYPE:
							ss.bpr = (act.amount * 100f) / potraise;
							// the opponent will need to call this much
							tocall = act.amount - lastbet;
							
						case Action.CALL_TYPE:
							// FIXME need a better way of getting eq, e.g. for hi/lo
							float eq = ss.meq != null ? ss.meq.totaleq / 100f : 0;
							int totalpot = hs.pot + act.amount + trail + tocall;
							ss.ev = totalpot * eq - act.amount;
							ss.tev += ss.ev;
							
							System.out.println("EV: pot=" + hs.pot + " actam=" + act.amount + " trail=" + trail + " tocall=" + tocall + " => total " + totalpot);
							System.out.println("EV:   eq=" + eq + " p*eq=" + (totalpot*eq) + " cost=" + act.amount + " => ev " + ss.ev);
							break;
							
						case Action.COLLECT_TYPE:
							// XXX should clear amounts of others who arn't winning, also remove from pot (leave rake)
							ss.won = true;
							ss.amount = -act.amount;
							break;
					}
					
					lastbet = Math.max(lastbet, ss.amount);
					ss.stack -= act.amount;
					trail += act.amount;
				}
				
				states.add(hs.clone());
			}
		}
		
		if (cache.size() == cacheSize) {
			cache.remove(0);
		}
		cache.add(states);
		
		return states;
	}

}