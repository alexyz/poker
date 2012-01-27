package pet;

import java.util.*;

/**
 * Utility methods
 */
public class Util {
	
	private static final Random r = new Random();
	
	/**
	 * pick a value from a (max length 64) that hasn't been picked before according to picked[0] and update picked[0]
	 */
	static String pick(String[] a, long[] picked) {
		int i;
		do {
			i = r.nextInt(a.length);
		} while ((picked[0] & (1L << i)) != 0);
		picked[0] |= (1L << i);
		return a[i];
	}

	/**
	 * Return array of cards that have not yet been dealt according to given board and hands
	 */
	public static String[] remdeck(String[] deck, String[] board, String[][] hands) {
		Set<String> s = new TreeSet<String>(Arrays.asList(deck));
		if (board != null) {
			rem(s, board);
		}
		if (hands != null) {
			for (String[] a : hands) {
				rem(s, a);
			}
		}
		return s.toArray(new String[s.size()]);
	}

	private static <T> void rem(Set<T> s, T[] a) {
		if (a != null) {
			for (T t : a) {
				if (t != null) {
					s.remove(t);
				}
			}
		}
	}

	/**
	 * Shuffle array contents
	 */
	public static void shuffle(Object[] a) {
		for (int n = 0; n < 2; n++) {
			for (int m = 0; m < a.length; m++) {
				int i = r.nextInt(a.length);
				Object t = a[m];
				a[m] = a[i];
				a[i] = t;
			}
		}
	}

}