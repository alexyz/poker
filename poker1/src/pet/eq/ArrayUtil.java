package pet.eq;

import java.util.*;

/**
 * utilities for arrays
 */
public class ArrayUtil {

	/**
	 * Sort the hand in place (Arrays.sort copies array).
	 */
	public static <T> void sort(T[] a, Comparator<T> cmp) {
		int l = a.length;
		while (l > 0) {
			int newl = 0;
			for (int n = 1; n < l; n++) {
				T x = a[n - 1];
				T y = a[n];
				if (x == null || y == null) {
					throw new RuntimeException("can't sort " + Arrays.asList(a));
				}
				if (cmp.compare(x, y) > 0) {
					newl = n;
					a[n - 1] = y;
					a[n] = x;
				}
			}
			l = newl;
		}
	}

	/**
	 * copy one array into other
	 */
	public static <T> void copy(T[] from, T[] to) {
		for (int n = 0; n < from.length; n++) {
			to[n] = from[n];
		}
	}

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
	public static String[] remove(String[] deck, String[] board, String[][] hands) {
		Set<String> s = new TreeSet<String>(Arrays.asList(deck));
		if (board != null) {
			remove1(s, board);
		}
		if (hands != null) {
			for (String[] a : hands) {
				remove1(s, a);
			}
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
