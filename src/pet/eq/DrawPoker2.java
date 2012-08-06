
package pet.eq;

import java.util.*;

/**
 * experimental draw poker functions
 */
public abstract class DrawPoker2 extends Poker {

	/** represents a possible draw and its average score */
	public static class Draw implements Comparable<Draw> {
		public final String[] hole;
		public float score;
		public Draw(String[] hole, float score) {
			this.hole = hole;
			this.score = score;
		}
		@Override
		public int compareTo(Draw other) {
			return (int) Math.signum(score - other.score);
		}
	}
	
	
	
	public static void main(String[] args) {
		/*
		for (float f = 0; f <= 1f; f += (1f / 128)) {
			i = (int) ((uniqueValues.length - 1) * f);
			System.out.println("f " + f + " hand " + Poker.valueString(uniqueValues[i]));
		}
		 */
		
		//avghand();
		
		//String[] x = new String[] { "Ah", "Ad", "2c", "3d", "4c" };
		//String[] x = new String[] { "7c", "7s", "Qs", "7d", "Jd" };
		//String[] x = new String[] { "Ah", "Kh", "Kc", "2h", "3h" };
		//String[] x = new String[] { "4c", "5h", "6d", "7s", "9h" };
		//String[] x = new String[] { "4h", "5s", "6d", "9c", "Th" };
		
		//String[] x = new String[] { "3h", "4c", "2d", "5c", "2s" };
		//String[] x = new String[] { "2h", "2d", "5h", "9h", "Jh" };
		//String[] x = new String[] { "8h", "7c", "6c", "5s", "4s" };
		//String[] x = new String[] { "Kc", "Ac", "Js", "3h", "7c" };
		//String[] x = new String[] { "4d", "6d", "5c", "3h", "5h" };
		//String[] x = new String[] { "Kd", "Ks", "Qh", "Jc", "Tc" };
		
		String[] x = new String[] { "2c", "5d", "4h", "8d", "4c" };
		
		List<Draw> l = new ArrayList<Draw>();
		
		for (int n = 0; n <= 5; n++) {
			String[] y = getDrawingHand(l, x, n, dsLowValue, 2f);
			String[] z = DrawPoker.getDraw(x, n);
			System.out.println("draw " + n + " old: " + Arrays.toString(z));
			System.out.println("       new: " + Arrays.toString(y));
		}
		
		Collections.sort(l);
		Collections.reverse(l);
		for (Draw d : l) {
			System.out.println(String.format("%.6f -> %s", d.score, Arrays.toString(d.hole)));
		}
		
	}
	
	/*
	private static void avghand() {
		String[] deck = Poker.deck.toArray(new String[52]);
		String[] hand = new String[5];
		int[] a = new int[2598960];
		int count = 0;
		for (int c1 = 0; c1 < deck.length; c1++) {
			//System.out.println(c1 + ", " + c2);
			hand[0] = deck[c1];
			for (int c2 = c1 + 1; c2 < deck.length; c2++) {
				hand[1] = deck[c2];
				for (int c3 = c2 + 1; c3 < deck.length; c3++) {
					hand[2] = deck[c3];
					for (int c4 = c3 + 1; c4 < deck.length; c4++) {
						hand[3] = deck[c4];
						for (int c5 = c4 + 1; c5 < deck.length; c5++) {
							hand[4] = deck[c5];
							int v = Poker.value(hand);
							a[count] = v;
							int v2 = Arrays.binarySearch(uniqueValues, v);
							if (v2 < 0) {
								throw new RuntimeException();
							}
							count++;
						}
					}
				}
			}
		}
		
		Arrays.sort(a);
		for (int n = 0; n <= 100; n++) {
			float f = n / 100f;
			int i = (int) ((a.length - 1) * f);
			System.out.println("hand " + f + " value " + Poker.valueString(a[i]));
		}
		
	}
	*/
	
	/**
	 * get normalised score of high hand (i.e. hand value is 0-1), optionally inverted
	 */
	protected static float score(int value, float bias, boolean high) {
		int[] uniqueValues = uniqueValues();
		int p = Arrays.binarySearch(uniqueValues, value);
		if (p < 0) {
			throw new RuntimeException();
		}
		if (!high) {
			// invert score for deuce to seven low
			p = uniqueValues.length - 1 - p;
		}
		return (float) Math.pow((1f * p) / (uniqueValues.length - 1f), bias);
	}
	
	public static String[] getDrawingHand(final String[] hand, final int drawn, boolean hi) {
		return getDrawingHand(null, hand, drawn, hi ? Poker.hiValue : Poker.dsLowValue, 2f);
	}

	/**
	 * get the best drawing hand for the given hand, number drawn, hand valuation and big hand bias.
	 * optionally returns score of all possible drawing hands.
	 */
	private static String[] getDrawingHand(List<Draw> draws, final String[] hand, final int drawn, Value value, float bias) {

		if (drawn < 0 || drawn > 5) {
			throw new RuntimeException();
			
		} else if (drawn == 5) {
			// special case, no draw and no meaningful score
			return new String[0];
			
		} else if (drawn == 0) {
			// special case, nothing to test other than given hand
			if (draws != null) {
				draws.add(new Draw(hand, value.score(hand, bias)));
			}
			return hand.clone();
		}
		
		// drawing 1-4
		
		// from players point of view, all other cards are possible
		final String[] deck = Poker.remdeck(null, hand);
		final String[] drawnHand = new String[5];
		final int imax = MathsUtil.bincoff(5, 5 - drawn);
		final int jmax = MathsUtil.bincoff(deck.length, drawn);
		
		String[] maxDrawingHand = null;
		float maxScore = -1f;
		
		for (int i = 0; i < imax; i++) {
			Arrays.fill(drawnHand, null);
			// pick kept from hand
			MathsUtil.kcomb(5 - drawn, i, hand, drawnHand, 0);
			float score = 0;
			
			for (int j = 0; j < jmax; j++) {
				// pick drawn from deck
				MathsUtil.kcomb(drawn, j, deck, drawnHand, 5 - drawn);
				score += value.score(drawnHand, bias);
			}
			
			float averageScore = score / (1.0f * jmax);
			String[] drawingHand = Arrays.copyOf(drawnHand, 5 - drawn);
			if (draws != null) {
				draws.add(new Draw(drawingHand, averageScore));
			}
					
			if (score > maxScore) {
				// copy new max hole cards
				maxDrawingHand = drawingHand;
				maxScore = score;
			}
		}
		
		return maxDrawingHand;
	}
	
}
