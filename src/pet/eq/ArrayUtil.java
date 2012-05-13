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


}
