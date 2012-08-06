package pet.hp;

import java.util.*;

import pet.eq.Cmp;
import pet.eq.DrawPoker;
import pet.eq.DrawPoker2;

/**
 * Utilities for hands (no analysis - see HandInfo)
 */
public class HandUtil {
	
	/** player hole cards as string array, plus metadata to indicate discarded cards and if cards were guessed */
	public static class HoleCards {
		/** hole cards for display/equity purposes */
		public final String[] hole;
		/** discarded cards if any */
		public final String[] discarded;
		/** are hole cards guessed */
		public final boolean guess;
		public HoleCards(String[] hole, String[] discarded, boolean guess) {
			this.hole = hole;
			this.discarded = discarded;
			this.guess = guess;
		}
		public HoleCards(int numHole, int numDiscarded) {
			this(new String[numHole], numDiscarded > 0 ? new String[numDiscarded] : null, false);
		}
		public HoleCards(String[] hole) {
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
			case Game.DSTD_TYPE:
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
	private static HoleCards kept(String[] hole1, String[] hole2, int discards) {
		HoleCards h = new HoleCards(5 - discards, discards);
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
	public static HandUtil.HoleCards getStreetHole(Hand hand, Seat seat, int street) {
		HoleCards hc = null;
		switch (hand.game.type) {
			case Game.DSTD_TYPE:
				if (street == GameUtil.getMaxStreets(hand.game.type) - 1) {
					// on final street just return final hand from seat
					hc = new HoleCards(seat.holeCards.clone());
					
				} else if (hand.myseat == seat) {
					// get current player cards but also see which ones were kept
					String[] x = hand.myHoleCards(street + 1);
					if (x == null) {
						x = hand.myseat.holeCards;
					}
					hc = kept(hand.myHoleCards(street), x, seat.drawn(street));
					
				} else {
					// guess opponents hole cards based on final hand
					String[] h = DrawPoker2.getDrawingHand(seat.holeCards, seat.drawn(street), false);
					hc = new HoleCards(h, null, true);
				}
				break;
				
			case Game.FCD_TYPE:
				if (street == 1) {
					// return final hand
					hc = new HoleCards(seat.holeCards.clone());
					
				} else if (hand.myseat == seat) {
					// return starting hand
					// XXX should also return discarded
					hc = kept(hand.myHoleCards0, seat.holeCards, seat.drawn0);
					
				} else if (seat.holeCards != null) {
					// guess what cards the opponent kept
					hc = new HoleCards(DrawPoker.getDrawingHand(seat.holeCards, seat.drawn0), null, true);
				}
				break;
				
			case Game.HE_TYPE:
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
				if (seat.holeCards != null) {
					hc = new HoleCards(seat.holeCards);
				}
				break;
				
			default:
				throw new RuntimeException("unknown game type " + hand.game);
		}
		if (hc != null) {
			Arrays.sort(hc.hole, Cmp.revCardCmp);
			if (hc.discarded != null) {
				Arrays.sort(hc.discarded, Cmp.revCardCmp);
			}
		}
		return hc;
	}
	
	/**
	 * Get all hole cards for hand
	 */
	public static String[][] getHoleCards(Hand hand) {
		List<String[]> holes = new ArrayList<String[]>();
		for (Seat seat : hand.seats) {
			if (seat.holeCards != null && seat.holeCards.length > 0) {
				holes.add(seat.holeCards);
			}
		}
		String[][] holesArr = holes.toArray(new String[holes.size()][]);
		return holesArr;
	}
	
	private HandUtil() {
		//
	}
	
}
