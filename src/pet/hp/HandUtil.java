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
	 * get board for street (index from 0).
	 */
	public static String[] getStreetBoard(Hand hand, int streetIndex) {
		switch (hand.game.type) {
			case Game.FCD_TYPE:
			case Game.DSTD_TYPE:
			case Game.DSSD_TYPE:
				return null;
			case Game.HE_TYPE:
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
				return streetIndex > 0 ? Arrays.copyOf(hand.board, streetIndex + 2) : null;
			case Game.STUD_TYPE:
			case Game.RAZZ_TYPE:
			case Game.STUDHL_TYPE:
				return streetIndex == 4 && hand.board != null ? hand.board : null;
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
	 * Get cards player had on this street
	 */
	public static HandUtil.HoleCards getCards(final Hand hand, final Seat seat, final int streetIndex) {
		if (seat.finalHoleCards == null && seat.finalUpCards == null) {
			// nothing to base return value on
			// note that only stud has up cards and the hole cards could be null
			return null;
		}
		
		HoleCards hc = null;
		
		switch (hand.game.type) {
			case Game.STUD_TYPE:
			case Game.STUDHL_TYPE:
			case Game.RAZZ_TYPE:
				String[] cards = getFinalCards(hand.game.type, seat);
				if (streetIndex < 4) {
					cards = Arrays.copyOf(cards, streetIndex + 3);
				}
				// don't sort (though could sort first two)
				return new HoleCards(cards);
			
			case Game.DSTD_TYPE:
			case Game.DSSD_TYPE:
				if (streetIndex == GameUtil.getMaxStreets(hand.game.type) - 1) {
					// on final street just return final hand from seat
					hc = new HoleCards(seat.finalHoleCards.clone());
					
				} else if (hand.myseat == seat) {
					// get current player cards but also see which ones were kept
					String[] x = hand.myDrawCards(streetIndex);
					String[] y = hand.myDrawCards(streetIndex + 1);
					System.out.println("my hole cards for street " + streetIndex + " are " + Arrays.toString(x) + " and " + Arrays.toString(y));
					if (y == null) {
						y = hand.myseat.finalHoleCards;
						System.out.println("using final hole cards " + Arrays.toString(y));
					}
					hc = kept(x, y, seat.drawn(streetIndex));
					
				} else {
					// guess opponents hole cards based on final hand
					String[] h = DrawPoker2.getDrawingHand(seat.finalHoleCards, seat.drawn(streetIndex), false);
					hc = new HoleCards(h, null, true);
				}
				break;
				
			case Game.FCD_TYPE:
				if (streetIndex == 1) {
					// return final hand
					hc = new HoleCards(seat.finalHoleCards.clone());
					
				} else if (hand.myseat == seat) {
					// return starting hand
					// XXX should also return discarded
					hc = kept(hand.myDrawCards0, seat.finalHoleCards, seat.drawn0);
					
				} else {
					// guess what cards the opponent kept
					hc = new HoleCards(DrawPoker.getDrawingHand(seat.finalHoleCards, seat.drawn0), null, true);
				}
				break;
				
			case Game.HE_TYPE:
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
				hc = new HoleCards(seat.finalHoleCards);
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
	 * get the final cards this seat had for display purposes returns null if no
	 * known cards, array may contain null if some are unknown
	 */
	public static String[] getFinalCards(int gametype, Seat seat) {
		switch (gametype) {
			case Game.STUD_TYPE:
			case Game.STUDHL_TYPE:
			case Game.RAZZ_TYPE:
				String[] holeCards = seat.finalHoleCards;
				String[] upCards = seat.finalUpCards;
				if (holeCards == null && upCards == null) {
					return null;
				}
				String[] cards = new String[7];
				if (holeCards != null) {
					cards[0] = holeCards.length > 0 ? holeCards[0] : null;
					cards[1] = holeCards.length > 1 ? holeCards[1] : null;
					cards[6] = holeCards.length > 2 ? holeCards[2] : null;
				}
				if (upCards != null) {
					cards[2] = upCards.length > 0 ? upCards[0] : null;
					cards[3] = upCards.length > 1 ? upCards[1] : null;
					cards[4] = upCards.length > 2 ? upCards[2] : null;
					cards[5] = upCards.length > 3 ? upCards[3] : null;
				}
				return cards;
			default:
				// TODO sort them?
				return seat.finalHoleCards;
		}
	}
	
	/**
	 * Get all final hole cards for hand and the blockers
	 */
	public static List<String[]> getFinalCards(Hand hand) {
		List<String[]> cardsList = new ArrayList<String[]>();
		for (Seat seat : hand.seats) {
			String[] cards = getFinalCards(hand.game.type, seat);
			if (cards != null) {
				cardsList.add(cards);
			}
		}
		return cardsList;
	}
	
	private HandUtil() {
		//
	}
	
}
