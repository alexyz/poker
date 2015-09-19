package pet.hp.state;

import java.util.*;

import pet.eq.*;
import pet.hp.*;

/**
 * creates hand state and seat state objects
 */
public class HandStateUtil {
	
	private static final ArrayList<List<HandState>> cache = new ArrayList<>();
	private static final int cacheSize = 100;
	
	/**
	 * Get the first seat state for each street for the given seat
	 */
	public static List<SeatState> getFirst(List<HandState> handStates, int seatNum) {
		ArrayList<SeatState> seatStates = new ArrayList<>();
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
		System.out.println("hand state util: get hand states for " + hand);
		// see if we've already done this hand
		for (List<HandState> l : cache) {
			if (l.get(0).hand.id.equals(hand.id)) {
				System.out.println("cache hit");
				return l;
			}
		}

		final List<HandState> states = new ArrayList<>();
		
		// initial state (not displayed)
		// will be reassigned after each action
		HandState hs = new HandState(hand);
		hs.pot = 0;
		hs.buttonIndex = hand.button - 1;
		for (Seat seat : hand.seats) {
			SeatState ss = new SeatState(seat);
			//ss.hole = HandUtil.getFinalCards(hand.game.type, seat);
			ss.stack = seat.chips;
			hs.seats[seat.num - 1] = ss;
		}
		
		// equity stuff
		final Poker poker = GameUtil.getPoker(hand.game.type);
		final int minHoleCards = poker.minHoleCards();
		final List<String[]> holeCards = new ArrayList<>();
		final List<SeatState> holeCardSeats = new ArrayList<>();
		final Set<String> blockers = new TreeSet<>();
		
		System.out.println("reshuf on " + hand.reshuffleStreetIndex);
		
		// for each street
		for (int s = 0; s < hand.streets.length; s++) {
			System.out.println("street index " + s);
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
				System.out.println("clearing blockers");
				// this actually can happen at any time during the draw
				// so the blockers apply to some hands and not others
				blockers.clear();
			}
			
			for (SeatState ss : hs.seats) {
				if (ss != null) {
					hs.pot += ss.amount;
					ss.amount = 0;
					ss.meq = null;
					ss.actionNum = 0;
					
					// get cards of seat
					//System.out.println("get cards for " + ss.seat.name); 
					String[] blockersArr = blockers.size() > 0 ? blockers.toArray(new String[blockers.size()]) : null;
					CardsState cs = CardsStateUtil.getCards(hand, ss.seat, s, blockersArr);
					//System.out.println("hole cards for seat " + ss.seat + " street " + s + " are " + hc);
					ss.cardsState = cs;
					
					if (cs != null) {
						// do we have enough cards for stud?
						// unknown down cards will be null (holdem/draw never have null cards)
						List<String> setCards = new ArrayList<>();
						for (String c : cs.cards) {
							if (c != null) {
								setCards.add(c);
							}
						}
						
						// do we have 2 hole cards for omaha? other games just require 1
						if (setCards.size() < minHoleCards || ss.folded) {
							for (String c : setCards) {
								if (hand.reshuffleStreetIndex > s) {
									// there are no reshuffles in omaha
									// so condition will always be true
									blockers.add(c);
								}
							}
							
						} else {
							// do it
							holeCards.add(setCards.toArray(new String[setCards.size()]));
							holeCardSeats.add(ss);
						}
						
						if (cs.discarded != null && hand.reshuffleStreetIndex > s) {
							// count all discards on all streets as blockers
							// though in triple draw the deck can be reshuffled mid-street
							for (String c : cs.discarded) {
								blockers.add(c);
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
			
			// draws depends on game and street
			int draws = GameUtil.getDraws(hand.game.type, s);
			List<String> boardList = hs.board != null ? Arrays.asList(hs.board) : null;
			// XXX save params here so it can be shown in equity ui page
			MEquity[] eqs = poker.equity(boardList, holeCards, blockers, draws);
			for (int n = 0; n < holeCardSeats.size(); n++) {
				SeatState ss = holeCardSeats.get(n);
				ss.meq = eqs[n];
			}
			
			// get default equity if there was no showdown
			for (SeatState ss : hs.seats) {
				if (ss != null) {
					if (hand.showdown) {
						ss.deq = ss.meq != null ? ss.meq.totaleq : 0f;
					} else {
						ss.deq = ss.seat.won > 0 ? 100f : 0f;
					}
				}
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
				
				if (act.type == Action.Type.FOLD) {
					ss.folded = true;
					ss.spr = 0;
					
				} else if (act.amount != 0) {
					// pot raise amount
					int potraise = hs.pot + trail + 2 * (lastbet - ss.amount);
					//System.out.println("MAX: pot=" + hs.pot + " trail=" + trail + " lastbet=" + lastbet + " committed=" + ss.amount + " => potraise " + potraise);
					
					ss.amount += act.amount;
					int tocall = 0;
					
					// actions that involve money
					// XXX ante/post?
					switch (act.type) {
						case BRINGSIN:
						case POST:
							// post might include dead blind, but it's pretty rare
						case BET:
						case RAISE:
							ss.bpr = (act.amount * 100f) / potraise;
							// the opponent will need to call this much
							tocall = act.amount - lastbet;
							
						case CALL:
							// use default equity if no showdown
							//float eq = ss.meq != null ? ss.meq.totaleq / 100f : 0;
							float eq = ss.deq / 100f;
							int totalpot = hs.pot + act.amount + trail + tocall;
							ss.ev = totalpot * eq - act.amount;
							ss.tev += ss.ev;
							
							//System.out.println("EV: pot=" + hs.pot + " actam=" + act.amount + " trail=" + trail + " tocall=" + tocall + " => total " + totalpot);
							//System.out.println("EV:   eq=" + eq + " p*eq=" + (totalpot*eq) + " cost=" + act.amount + " => ev " + ss.ev);
							break;
							
						case COLLECT:
							// XXX should clear amounts of others who arn't winning, also remove from pot (leave rake)
							ss.won = true;
							ss.amount = -act.amount;
							break;
							
						case ANTE:
						case CHECK:
						case DOESNTSHOW:
						case DRAW:
						case FOLD:
						case MUCK:
						case SHOW:
						case STANDPAT:
						case UNCALL:
						default:
							// do nothing
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
		
		System.out.println("hand state util: returning " + states.size() + " hand states");
		return states;
	}
	
}
