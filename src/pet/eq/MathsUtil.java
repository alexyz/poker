package pet.eq;

import java.math.BigInteger;

/**
 * Mathematical utility methods
 */
public class MathsUtil {
	
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
	public static int bincoff(int n, int k) {
		int c = C[n][k];
		if (c == -1) {
			throw new RuntimeException("no binary coefficient for " + n + ", " + k);
		}
		return c;
	}
	
	/**
	 * Combinatorial number system.
	 * Get the k combination at position p and write from 'from' into 'to' at offset.
	 */
	public static void kcomb(final int k, int p, final Object[] from, Object[] to, final int off) {
		//System.out.println("kcomb(" + k + "," + p + "," + from.length + "," + to.length + "," + off + ")");
		// for each digit (starting at the last)
		for (int b = k; b >= 1; b--) {
			// find biggest bin coff that will fit p
			for (int a = b - 1; a < 100; a++) {
				int x = bincoff(a, b);
				if (x > p) {
					// this is too big, so the last one must have fit
					p -= bincoff(a - 1, b);
					to[b - 1 + off] = from[a - 1];
					break;
				}
			}
		}
	}
	
}
