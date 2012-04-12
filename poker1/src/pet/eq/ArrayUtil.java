package pet.eq;

import java.util.*;

/**
 * utilities for arrays
 */
public class ArrayUtil {

	/**
	 * join two arrays together
	 */
	public static <T> T[] join(T[] a, T[] b) {
		T[] x = Arrays.copyOf(a, a.length + b.length);
		for (int n = 0; n < b.length; n++) {
			x[a.length + n] = b[n];
		}
		return x;
	}

	/**
	 * Return array of cards that have not yet been dealt according to given board and hands
	 */
	public static String[] remove(String[] deck, String[] board, String[][] hands, String[] blockers) {
		Set<String> s = new TreeSet<String>(Arrays.asList(deck));
		if (board != null) {
			remove1(s, board);
		}
		if (hands != null) {
			for (String[] a : hands) {
				remove1(s, a);
			}
		}
		if (blockers != null) {
			remove1(s, blockers);
		}
		return s.toArray(new String[s.size()]);
	}

	private static <T> void remove1(Set<T> s, T[] a) {
		if (a != null) {
			for (T t : a) {
				if (t != null) {
					s.remove(t);
				}
			}
		}
	}

}
