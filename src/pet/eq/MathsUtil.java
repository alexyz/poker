package pet.eq;

import java.math.BigInteger;

/**
 * Mathematical utility methods
 */
public class MathsUtil {
	
	private static final int[][] C = makeBinaryCoefficients(52, 52);
	
	/**
	 * Factorial (slow)
	 */
	public static BigInteger factorial(int n) {
		return n <= 1 ? BigInteger.ONE : BigInteger.valueOf(n).multiply(factorial(n - 1));
	}
	
	/**
	 * Binomial coefficient (slow)
	 */
	public static BigInteger binaryCoefficient(int n, int k) {
		return n == 0 ? BigInteger.ZERO : factorial(n).divide(factorial(k).multiply(factorial(n - k)));
	}
	
	/**
	 * Calculate binomial coefficients
	 */
	private static int[][] makeBinaryCoefficients(int nm, int km) {
		BigInteger max = BigInteger.valueOf(Integer.MAX_VALUE);
		int[][] r = new int[nm + 1][km + 1];
		for (int n = 0; n <= nm; n++) {
			for (int k = 0; k <= km; k++) {
				BigInteger v = binaryCoefficient(n, k);
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
	public static int binaryCoefficientFast(int n, int k) {
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
	public static void kCombination(final int k, int p, final Object[] from, Object[] to, final int off) {
		//System.out.println("kcomb(" + k + "," + p + "," + from.length + "," + to.length + "," + off + ")");
		// for each digit (starting at the last)
		for (int b = k; b >= 1; b--) {
			// find biggest bin coff that will fit p
			for (int a = b - 1; a < 100; a++) {
				int x = binaryCoefficientFast(a, b);
				if (x > p) {
					// this is too big, so the last one must have fit
					p -= binaryCoefficientFast(a - 1, b);
					to[b - 1 + off] = from[a - 1];
					break;
				}
			}
		}
	}
	
	public static float trunc(float f) {
		return Math.round(f);
	}
	
	public static Float trunc(Float f) {
		return f != null ? trunc(f.floatValue()) : null;
	}
	
	public static float round(float f, int dp) {
		float pow = (float) Math.pow(10, dp);
		float round = Math.round(f * pow);
		return round / pow;
	}
	
	public static Float round(Float f, int dp) {
		return f != null ? round(f.floatValue(), dp) : null;
	}
}
