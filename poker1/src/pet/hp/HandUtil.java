package pet.hp;

import java.text.NumberFormat;
import java.util.*;

import pet.eq.Cmp;

/**
 * Utilities for hands (no analysis - see HandInfo)
 */
public class HandUtil {
	
	/**
	 * Compare hands by id
	 */
	public static final Comparator<Hand> idCmp = new Comparator<Hand>() {
		@Override
		public int compare(Hand h1, Hand h2) {
			long cl = h1.id - h2.id;
			int c = cl > 0 ? 1 : cl == 0 ? 0 : -1;
			int d = h1.date.compareTo(h2.date);
			if (d != 0 && c != d) {
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
	 * get board for street
	 */
	public static String[] getStreetBoard(Hand hand, int street) {
		switch (hand.game.type) {
			case Game.FCD_TYPE:
				return null;
			case Game.HE_TYPE:
			case Game.OM_TYPE:
				return street > 0 ? Arrays.copyOf(hand.board, street + 2) : null;
		}
		throw new RuntimeException("unknown game type " + hand.game.type);
	}
	
	/**
	 * this method is tragically naive
	 */
	private static String[] kept(String[] hole1, String[] hole2, int discards) {
		String[] kept = new String[5 - discards];
		int i = 0;
		for (int n = 0; n < 5; n++) {
			for (int m = 0; m < 5; m++) {
				if (hole1[n].equals(hole2[m])) {
					kept[i++] = hole1[n];
					break;
				}
			}
		}
		return kept;
	}

	/**
	 * Get hole cards player had on this street (if they changed)
	 */
	public static String[] getStreetHole(Hand hand, Seat seat, int street) {
		String[] h = null;
		switch (hand.game.type) {
			case Game.FCD_TYPE:
				if (street == 1) {
					// return final hand
					h = seat.hole.clone();
					
				} else if (hand.myseat == seat) {
					// return starting hand
					h = kept(hand.myhole, seat.hole, seat.discards);
					
				} else if (seat.hole != null) {
					// guess what cards the opponent kept
					h = new String[5 - seat.discards];
					for (int n = 0; n < h.length; n++) {
						h[n] = seat.hole[n];
					}
				}
				break;
			case Game.HE_TYPE:
			case Game.OM_TYPE:
				if (seat.hole != null) {
					h = seat.hole.clone();
				}
				break;
			default:
				throw new RuntimeException("unknown game type " + hand.game);
		}
		if (h != null) {
			Arrays.sort(h, Cmp.revCardCmp);
		}
		return h;
	}

	/**
	 * Return string describing action (but not player)
	 */
	public static String actionString(Hand hand, Action action) {
		StringBuilder sb = new StringBuilder();
		sb.append(Action.TYPENAME[action.type]);
		if (action.amount > 0) {
			sb.append(" ").append(GameUtil.formatMoney(hand.game.currency, action.amount));
		}
		if (action.seat.discards > 0) {
			sb.append(" discards ").append(action.seat.discards);
		}
		if (action.allin) {
			sb.append(" all in");
		}
		return sb.toString();
	}

}
