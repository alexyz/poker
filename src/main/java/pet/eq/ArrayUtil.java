package pet.eq;

import java.util.*;

/**
 * utilities for arrays.
 * Note: if you want subarrays, use Arrays.copyOfRange
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
	public static String pick(Random r, String[] a, long[] picked) {
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
	
	/**
	 * subtract b from a
	 */
	public static String[] sub(String[] a, String[] b) {
		// really inefficient...
		TreeSet<String> s = new TreeSet<>(Arrays.asList(a));
		s.removeAll(Arrays.asList(b));
		if (s.size() != a.length) {
			//System.out.println("sub: " + Arrays.toString(a) + " - " + Arrays.toString(b) + " = " + s);
			return s.toArray(new String[s.size()]);
		} else {
			return a;
		}
	}
	
	public static void main (String[] args) {
		Random r = new Random();
		for (int n = 0; n < 10; n++) {
			int[] a = new int[r.nextInt(5) + 1];
			for (int i = 0; i < a.length; i++) {
				a[i] = r.nextInt(10);
			}
			int[] b = a.clone();
			sort(b);
			System.out.println(Arrays.toString(a) + " => " + Arrays.toString(b));
		}
	}
	
	/**
	 * sort a small array of numbers, more efficiently than Arrays.sort
	 */
	public static void sort (int[] a) {
		// simple insertion sort derived from wikipedia
		for (int i = 1; i < a.length; i++) {
			int v = a[i];
			int h = i;
			while (h > 0 && v < a[h - 1]) {
				a[h] = a[h - 1];
				h--;
			}
			a[h] = v;
		}
	}
	
}
