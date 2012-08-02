package pet.hp;

import java.util.*;

import pet.eq.Cmp;
import pet.eq.DrawPoker;

/**
 * Utilities for hands (no analysis - see HandInfo)
 */
public class HandUtil {
	
	public static class Hole {
		/** hole cards for display/equity purposes */
		public final String[] hole;
		/** discarded cards if any */
		public final String[] discarded;
		/** are hole cards guessed */
		public final boolean guess;
		public Hole(String[] hole, String[] discarded, boolean guess) {
			this.hole = hole;
			this.discarded = discarded;
			this.guess = guess;
		}
		public Hole(int numHole, int numDiscarded) {
			this(new String[numHole], numDiscarded > 0 ? new String[numDiscarded] : null, false);
		}
		public Hole(String[] hole) {
			this(hole, null, false);
		}
	}
	
	/**
	 * Compare hands by id
	 */
	public static final Comparator<Hand> idCmp = new Comparator<Hand>() {
		@Override
		public int compare(Hand h1, Hand h2) {
			int c = h1.id.compareTo(h2.id);
			int d = h1.date.compareTo(h2.date);
			if (c != d) {
				throw new RuntimeException("date/id mismatch: " + h1 + " and " + h2);
			}
			return c;
		}
	};
	
	public static final Comparator<Seat> seatCmp = new Comparator<Seat>() {
		@Override
		public int compare(Seat s1, Seat s2) {
			return s1.num - s2.num;
		}
	};
	
	/**
	 * get board for street.
	 * Always returns new array.
	 */
	public static String[] getStreetBoard(Hand hand, int street) {
		switch (hand.game.type) {
			case Game.FCD_TYPE:
				return null;
			case Game.HE_TYPE:
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
				return street > 0 ? Arrays.copyOf(hand.board, street + 2) : null;
			default:
				throw new RuntimeException("unknown game type " + hand.game.type);
		}
	}
	
	/**
	 * get the hole cards the current player kept and discarded
	 */
	private static Hole kept(String[] hole1, String[] hole2, int discards) {
		Hole h = new Hole(5 - discards, discards);
		int i = 0, j = 0;
		for (int n1 = 0; n1 < 5; n1++) {
			find: {
				for (int n2 = 0; n2 < 5; n2++) {
					if (hole1[n1].equals(hole2[n2])) {
						h.hole[i++] = hole1[n1];
						break find;
					}
				}
				h.discarded[j++] = hole1[n1];
			}
		}
		return h;
	}
	
	/**
	 * Get hole cards player had on this street (if they changed).
	 */
	public static HandUtil.Hole getStreetHole(Hand hand, Seat seat, int street) {
		Hole h = null;
		switch (hand.game.type) {
			case Game.FCD_TYPE:
				if (street == 1) {
					// return final hand
					h = new Hole(seat.hole.clone());
					
				} else if (hand.myseat == seat) {
					// return starting hand
					// XXX should also return discarded
					h = kept(hand.myhole, seat.hole, seat.discards);
					
				} else if (seat.hole != null) {
					// guess what cards the opponent kept
					h = new Hole(DrawPoker.getDrawingHand(seat.hole, seat.discards), null, true);
				}
				break;
				
			case Game.HE_TYPE:
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
				if (seat.hole != null) {
					h = new Hole(seat.hole);
				}
				break;
				
			default:
				throw new RuntimeException("unknown game type " + hand.game);
		}
		if (h != null) {
			Arrays.sort(h.hole, Cmp.revCardCmp);
			if (h.discarded != null) {
				Arrays.sort(h.discarded, Cmp.revCardCmp);
			}
		}
		return h;
	}
	
	/**
	 * Return string describing action (but not player)
	 */
	public static String actionString(Hand hand, Action action) {
		StringBuilder sb = new StringBuilder();
		sb.append(Action.TYPENAME[action.type]);
		if (action.type == Action.DRAW_TYPE) {
			sb.append(" ").append(action.seat.discards);
		} else if (action.amount != 0) {
			sb.append(" ").append(GameUtil.formatMoney(hand.game.currency, action.amount));
			if (action.allin) {
				sb.append(" all in");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Get all hole cards for hand
	 */
	public static String[][] getHoleCards(Hand hand) {
		List<String[]> holes = new ArrayList<String[]>();
		for (Seat seat : hand.seats) {
			if (seat.hole != null && seat.hole.length > 0) {
				holes.add(seat.hole);
			}
		}
		String[][] holesArr = holes.toArray(new String[holes.size()][]);
		return holesArr;
	}
	
	private HandUtil() {
		//
	}
	
}
