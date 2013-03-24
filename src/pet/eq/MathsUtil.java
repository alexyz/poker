package pet.eq;

import java.math.BigInteger;
import java.util.List;

/**
 * Mathematical utility methods
 */
public class MathsUtil {
	
	/**
	 * convert list/array to []
	 * convert map to { }
	 */
	public static void main(String[] args) {
		final BigInteger max = BigInteger.valueOf(Long.MAX_VALUE);
		StringBuilder sb = new StringBuilder();
		for (int n = 0; n < 52; n++) {
			if (n > 0) {
				sb.append(",\n  ");
			}
			sb.append("[");
			for (int m = 0; m < 52; m++) {
				if (m > 0) {
					sb.append(", ");
				}
				sb.append(String.format("%15d", binaryCoefficient(n, m).longValue()));
			}
			sb.append("]");
		}
		System.out.println(sb);
	}
	
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
	
	public static abstract class Combination {
		public String[] src;
		public String[] dest;
		public int pick;
		public int off;
		public final void run() {
			run(0, pick);
		}
		private void run (int n, int p) {
			if (p == 0) {
				apply();
			} else {
				int m = src.length - pick;
				while (n <= m) {
					dest[off+pick-1] = src[n];
					run(n+1, p-1);
					n++;
				}
			}
		}
		public abstract void apply();
	}
	
}
