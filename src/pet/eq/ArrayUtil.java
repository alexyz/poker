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
	 * pick a value from a (max length 63) that hasn't been picked before
	 * according to picked[0] and update picked[0]
	 */
	static String pick(Random r, String[] a, long[] picked) {
		if (a.length > 63) {
			throw new RuntimeException("array is longer than 63");
		}
		if (picked[0] >= ((1L << a.length) - 1)) {
			throw new RuntimeException("none left to pick");
		}
		while (true) {
			int i = r.nextInt(a.length);
			long m = 1L << i;
			if ((picked[0] & m) == 0) {
				picked[0] |= m;
				return a[i];
			}
		}
	}
	
	/**
	 * shuffle array
	 */
	public static void shuffle(Object[] a, Random r) {
		for (int n = 0; n < a.length; n++) {
			// don't just pick random position!
			int x = r.nextInt(a.length - n) + n;
			Object o = a[n];
			a[n] = a[x];
			a[x] = o;
		}
	}
	
}
