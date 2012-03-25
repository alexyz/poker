package pet.hp;

import java.util.*;

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
	public static final char FCD_TYPE = '5', HE_TYPE = 'H', OM_TYPE = 'O';
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
	public static String getStreetName (char type, int s) {
		switch (type) {
		case FCD_TYPE:
			return drawstreetnames[s];
		case HE_TYPE:
		case OM_TYPE:
			return hestreetnames[s];
		}
		throw new RuntimeException("unknown game type " + type);
	}
	
	/**
	 * get board for street
	 */
	public static String[] getStreetBoard(String[] board, int s) {
		return s > 0 ? Arrays.copyOf(board, s + 2) : null;
	}
	
}