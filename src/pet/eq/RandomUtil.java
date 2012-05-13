package pet.eq;

import java.util.Random;

public class RandomUtil {

	// this is somewhat thread safe
	private static final Random random = new Random();
	
	/**
	 * pick a value from a (max length 64) that hasn't been picked before
	 * according to picked[0] and update picked[0]
	 */
	@Deprecated
	static String pick(String[] a, long[] picked) {
		int i;
		do {
			i = random.nextInt(a.length);
		} while ((picked[0] & (1L << i)) != 0);
		picked[0] |= (1L << i);
		return a[i];
	}

}
