package pet;

import java.math.BigInteger;

/**
 * Mathematical utility methods
 */
public class MUtil {
	
	private static final int[][] C = mkbc(52, 52);

	/**
	 * Factorial (slow)
	 */
	private static BigInteger facslow(int n) {
		return n <= 1 ? BigInteger.ONE : BigInteger.valueOf(n).multiply(facslow(n - 1));
	}

	/**
	 * Binomial coefficient (slow)
	 */
	private static BigInteger bincoffslow(int n, int k) {
		return n == 0 ? BigInteger.ZERO : facslow(n).divide(facslow(k).multiply(facslow(n - k)));
	}
	
	/**
	 * Calculate binomial coefficients
	 */
	private static int[][] mkbc(int nm, int km) {
		BigInteger max = BigInteger.valueOf(Integer.MAX_VALUE);
		int[][] r = new int[nm + 1][km + 1];
		for (int n = 0; n <= nm; n++) {
			for (int k = 0; k <= km; k++) {
				BigInteger v = bincoffslow(n, k);
				if (v.compareTo(max) > 0) {
					r[n][k] = -1;
				} else {
					r[n][k] = v.intValue();
				}
			}
		}
		return r;
	}
	
	/**
	 * Return cached binomial coefficient (n pick k).
	 * I.e. how many ways can you pick k objects from n
	 */
	static int bincoff(int n, int k) {
		int c = C[n][k];
		if (c == -1) {
			throw new RuntimeException();
		}
		return c;
	}

}
