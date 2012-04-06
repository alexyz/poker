package pet.hp;

import java.text.NumberFormat;
import java.util.*;

/**
 * Utilities for hands (no analysis - see HandInfo)
 */
public class HandUtil {
	public static final char FCD_TYPE = 'F', HE_TYPE = 'H', OM_TYPE = 'O';
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
	
	public static final String[] hestreetnames = { "Pre flop", "Flop", "Turn", "River" };
	public static final String[] drawstreetnames = { "Pre draw", "Post draw" };

	/** return true if this street is the showdown street for the given game type */
	public static boolean isShowdown (char type, int street) {
		switch (type) {
			case FCD_TYPE:
				return street == drawstreetnames.length - 1;
			case HE_TYPE:
			case OM_TYPE:
				return street == hestreetnames.length - 1;
		}
		throw new RuntimeException("unknown game type " + type);
	}

	/** return the maximum number of streets in this game type */
	public static int getMaxStreets (char type) {
		switch (type) {
			case FCD_TYPE:
				return drawstreetnames.length;
			case HE_TYPE:
			case OM_TYPE:
				return hestreetnames.length;
		}
		throw new RuntimeException("unknown game type " + type);
	}

	/** get the name of the street for this game type */
	public static String getStreetName (char type, int street) {
		switch (type) {
			case FCD_TYPE:
				return drawstreetnames[street];
			case HE_TYPE:
			case OM_TYPE:
				return hestreetnames[street];
		}
		throw new RuntimeException("unknown game type " + type);
	}

	/**
	 * get board for street
	 */
	public static String[] getStreetBoard(Hand hand, int street) {
		switch (hand.game.type) {
			case FCD_TYPE:
				return null;
			case HE_TYPE:
			case OM_TYPE:
				return street > 0 ? Arrays.copyOf(hand.board, street + 2) : null;
		}
		throw new RuntimeException("unknown game type " + hand.game);
	}

	public static String formatMoney(char currency, int amount) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		if (currency != 0) {
			return String.format("%c%.2f", currency, amount / 100f);
		} else {
			return nf.format(amount);
		}
	}
	
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
	 * Get hole cards player had on this street
	 */
	public static String[] getStreetHole(Hand hand, Seat seat, int street) {
		switch (hand.game.type) {
			case FCD_TYPE:
				if (street == 1) {
					// return final hand
					return seat.hole;
				}
				if (hand.myseat == seat) {
					// return starting hand
					return kept(hand.myhole, seat.hole, seat.discards);
				}
				if (seat.hole != null) {
					// guess what cards the opponent kept
					String[] guessedHole = new String[5 - seat.discards];
					for (int n = 0; n < guessedHole.length; n++) {
						guessedHole[n] = seat.hole[n];
					}
					return guessedHole;
				}
				return null;
			case HE_TYPE:
			case OM_TYPE:
				return seat.hole;
		}
		throw new RuntimeException("unknown game type " + hand.game);
	}

	/**
	 * Return string describing action (but not player)
	 */
	public static String actionString(Hand hand, Action action) {
		StringBuilder sb = new StringBuilder();
		sb.append(Action.TYPENAME[action.type]);
		if (action.amount > 0) {
			sb.append(" ").append(formatMoney(hand.game.currency, action.amount));
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
