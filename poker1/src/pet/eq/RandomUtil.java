package pet.eq;

import java.util.Random;

public class RandomUtil {

	private static final Random random = new Random();
	
	/**
	 * pick a value from a (max length 64) that hasn't been picked before
	 * according to picked[0] and update picked[0]
	 */
	static String pick(String[] a, long[] picked) {
		int i;
		do {
			i = random.nextInt(a.length);
		} while ((picked[0] & (1L << i)) != 0);
		picked[0] |= (1L << i);
		return a[i];
	}

	/**
	 * Shuffle array contents
	 */
	public static void shuffle(Object[] a) {
		for (int n = 0; n < 2; n++) {
			for (int m = 0; m < a.length; m++) {
				int i = random.nextInt(a.length);
				Object t = a[m];
				a[m] = a[i];
				a[i] = t;
			}
		}
	}

}
