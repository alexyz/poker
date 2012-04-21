package pet.hp;

import java.util.*;

import pet.eq.Cmp;
import pet.eq.DrawPoker;

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
		}
		throw new RuntimeException("unknown game type " + hand.game.type);
	}
	
	/**
	 * get the hole cards the player kept.
	 * Always returns new array
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
	 * Get hole cards player had on this street (if they changed).
	 * Always returns new array
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
					h = DrawPoker.getHand(seat.hole, seat.discards);
				}
				break;
			case Game.HE_TYPE:
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
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

}
