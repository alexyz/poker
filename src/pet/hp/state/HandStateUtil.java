package pet.hp.state;

import java.util.*;

import pet.eq.*;
import pet.hp.*;
import pet.hp.HandUtil.HoleCards;

public class HandStateUtil {
	
	private static final ArrayList<List<HandState>> cache = new ArrayList<List<HandState>>();
	private static final int cacheSize = 10;
	
	/**
	 * Get the first seat state for each street for the given seat
	 */
	public static List<SeatState> getFirst(List<HandState> handStates, int seatNum) {
		ArrayList<SeatState> seatStates = new ArrayList<SeatState>();
		int s = -1;
		for (HandState hs : handStates) {
			SeatState as = hs.actionSeat();
			if (as != null && as.seat.num == seatNum) {
				if (hs.streetIndex > s) {
					seatStates.add(as);
					s = hs.streetIndex;
				}
			}
		}
		return seatStates;
	}
	
	/**
	 * convert hand into list of hand states
	 */
	// synchronize for cache, but sampled equity calc can be slow...
	public static synchronized List<HandState> getStates(final Hand hand) {
		System.out.println("get hands states for " + hand);
		// see if we've already done this hand
		for (List<HandState> l : cache) {
			if (l.get(0).hand.id.equals(hand.id)) {
				System.out.println("cache hit");
				return l;
			}
		}
		
		final List<HandState> states = new ArrayList<HandState>();
		
		// initial state (not displayed)
		// will be reassigned after each action
		HandState hs = new HandState(hand);
		hs.pot = hand.antes;
		hs.buttonIndex = hand.button - 1;
		for (Seat seat : hand.seats) {
			SeatState ss = new SeatState(seat);
			//ss.hole = HandUtil.getFinalCards(hand.game.type, seat);
			ss.stack = seat.chips;
			hs.seats[seat.num - 1] = ss;
		}
		
		// equity stuff
		final Poker poker = GameUtil.getPoker(hand.game);
		final int minHoleCards = GameUtil.getMinHoleCards(hand.game.type);
		final List<String[]> holeCards = new ArrayList<String[]>();
		final List<SeatState> holeCardSeats = new ArrayList<SeatState>();
		final Set<String> blockers = new TreeSet<String>();
		
		// for each street
		for (int s = 0; s < hand.streets.length; s++) {
			
			//
			// state for clear bets, place card
			//
			final String[] board = HandUtil.getStreetBoard(hand, s);
			hs.board = board;
			hs.note = GameUtil.getStreetName(hand.game.type, s);
			hs.action = null;
			hs.streetIndex = s;
			hs.actionSeatIndex = -1;
			holeCards.clear();
			holeCardSeats.clear();
			if (hand.reshuffleStreetIndex == s) {
				blockers.clear();
			}
			
			for (SeatState ss : hs.seats) {
				if (ss != null) {
					hs.pot += ss.amount;
					ss.amount = 0;
					ss.meq = null;
					ss.actionNum = 0;
					
					// get cards of seat
					HoleCards hc = HandUtil.getCards(hand, ss.seat, s);
					ss.holeObj = hc;
					
					if (hc != null) {
						// do we have enough cards for stud?
						// unknown down cards will be null (holdem/draw never have null cards)
						boolean partial = false;
						for (int n = 0; n < hc.hole.length; n++) {
							if (hc.hole[n] == null) {
								partial = true;
								break;
							}
						}
						
						// do we have 2 hole cards for omaha? other games just require 1
						if (partial || hc.hole.length < minHoleCards || ss.folded) {
							for (int n = 0; n < hc.hole.length; n++) {
								if (hc.hole[n] != null) {
									blockers.add(hc.hole[n]);
								}
							}
							
						} else {
							// do it
							holeCards.add(hc.hole);
							holeCardSeats.add(ss);
						}
						
						if (hc.discarded != null && hand.reshuffleStreetIndex > s) {
							// count all discards on all streets as blockers
							// though in triple draw the deck can be reshuffled mid-street
							for (int n = 0; n < hc.discarded.length; n++) {
								blockers.add(hc.discarded[n]);
							}
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
			
			String[][] holeCardsArr = holeCards.toArray(new String[holeCards.size()][]);
			String[] blockersArr = blockers.toArray(new String[blockers.size()]);
			MEquity[] eqs = poker.equity(hs.board, holeCardsArr, blockersArr);
			for (int n = 0; n < holeCardSeats.size(); n++) {
				SeatState ss = holeCardSeats.get(n);
				ss.meq = eqs[n];
			}
			
			states.add(hs.clone());
			
			//
			// states for player actions for street
			//
			
			int trail = 0;
			int lastbet = 0;
			for (Action act : hand.streets[s]) {
				//System.out.println();
				//System.out.println("act " + act);
				hs = hs.clone();
				hs.action = act;
				
				hs.actionSeatIndex = act.seat.num - 1;
				SeatState ss = hs.actionSeat();
				ss.bpr = 0;
				ss.ev = 0;
				ss.actionNum++;
				
				if (act.type == Action.FOLD_TYPE) {
					ss.folded = true;
					ss.spr = 0;
					
				} else if (act.amount != 0) {
					// pot raise amount
					int potraise = hs.pot + trail + 2 * (lastbet - ss.amount);
					//System.out.println("MAX: pot=" + hs.pot + " trail=" + trail + " lastbet=" + lastbet + " committed=" + ss.amount + " => potraise " + potraise);
					
					ss.amount += act.amount;
					int tocall = 0;
					
					switch (act.type) {
						case Action.BRINGSIN_TYPE:
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
							
							//System.out.println("EV: pot=" + hs.pot + " actam=" + act.amount + " trail=" + trail + " tocall=" + tocall + " => total " + totalpot);
							//System.out.println("EV:   eq=" + eq + " p*eq=" + (totalpot*eq) + " cost=" + act.amount + " => ev " + ss.ev);
							break;
							
						case Action.COLLECT_TYPE:
							// XXX should clear amounts of others who arn't winning, also remove from pot (leave rake)
							ss.won = true;
							ss.amount = -act.amount;
							break;
							
						default:
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
