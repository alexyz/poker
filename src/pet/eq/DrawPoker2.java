
package pet.eq;

import java.util.*;

public class DrawPoker2 {
	
	private static final int[] uniqueValues;
	
	static {
		uniqueValues = getUniqueValues();
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
		//String[] x = new String[] { "2h", "2d", "5h", "8h", "Jh" };
		//String[] x = new String[] { "8h", "7c", "6c", "5s", "4s" };
		String[] x = new String[] { "2d", "4h", "6d", "Qd", "7c" };
		
		for (int n = 0; n <= 5; n++) {
			String[] y = getDraw(x, n);
			String[] z = DrawPoker.getDraw(x, n);
			System.out.println("draw " + n + " old: " + Arrays.toString(z));
			System.out.println("       new: " + Arrays.toString(y));
		}
		
	}
	
	/**
	 * Go through every possible 5 card hand and collect the unique hand values in order
	 */
	private static int[] getUniqueValues() {
		// TODO this is not very efficient, could just serialise/deserialise array
		
		Set<Integer> uniqueValueSet = new TreeSet<Integer>();
		String[] deck = Poker.deck.toArray(new String[Poker.deck.size()]);
		String[] hand = new String[5];
		int valueCount = 0;
		for (int n0 = 0; n0 < deck.length; n0++) {
			hand[0] = deck[n0];
			for (int n1 = n0 + 1; n1 < deck.length; n1++) {
				hand[1] = deck[n1];
				for (int n2 = n1 + 1; n2 < deck.length; n2++) {
					hand[2] = deck[n2];
					for (int n3 = n2 + 1; n3 < deck.length; n3++) {
						hand[3] = deck[n3];
						for (int n4 = n3 + 1; n4 < deck.length; n4++) {
							hand[4] = deck[n4];
							uniqueValueSet.add(Poker.value(hand));
							valueCount++;
						}
					}
				}
			}
		}
		System.out.println("values: " + valueCount);
		System.out.println("unique values: " + uniqueValueSet.size());
		
		int[] uniqueValues = new int[uniqueValueSet.size()];
		int i = 0;
		for (int v : uniqueValueSet) {
			uniqueValues[i++] = v;
		}
		Arrays.sort(uniqueValues);
		return uniqueValues;
	}
	
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
	
	
	public static String[] getDraw(final String[] hand, final int drawn) {
		if (drawn < 0 || drawn > 5) {
			throw new RuntimeException();
		}
		
		//if (drawn == 0) {
		//return hand.clone();
		//}
		
		if (drawn == 5) {
			return new String[0];
		}
		
		if (uniqueValues == null) {
			getUniqueValues();
		}
		
		// FIXME if they draw 1 and result is hc/pa then assume they were drawing to str/fl
		// assuming there is a str/fl draw...
		final int minv;
		final int handv = Poker.value(hand);
		if (drawn == 1 && handv < Poker.TP_MASK) {
			minv = Poker.ST_MASK;
		} else {
			minv = 0;
		}
		
		// from players point of view, all other cards are possible
		final String[] deck = Poker.remdeck(null, hand);
		final String[] h = new String[5];
		final String[] maxh = new String[5 - drawn];
		final int pmax = MathsUtil.bincoff(5, 5 - drawn);
		final int qmax = MathsUtil.bincoff(deck.length, drawn);
		int maxscore = 0;
		
		for (int p = 0; p < pmax; p++) {
			Arrays.fill(h, null);
			// pick kept from hand
			MathsUtil.kcomb(5 - drawn, p, hand, h, 0);
			int score = 0;
			
			for (int q = 0; q < qmax; q++) {
				// pick drawn from deck
				MathsUtil.kcomb(drawn, q, deck, h, 5 - drawn);
				int v = Poker.value(h);
				if (v > minv) {
					int v2 = Arrays.binarySearch(uniqueValues, v);
					if (v2 < 0) {
						throw new RuntimeException();
					}
					score += v2;
				}
			}
			
			if (score > maxscore) {
				// copy new max hole cards
				for (int n = 0; n < 5 - drawn; n++) {
					maxh[n] = h[n];
				}
				maxscore = score;
				System.out.println("hand " + Arrays.toString(maxh) + " score " + score + " avg: " + (score / (1.0 * qmax * (uniqueValues.length - 1))));
			}
			
		}
		
		for (String c : maxh) {
			if (c == null) {
				throw new RuntimeException("could not get draw for " + Arrays.toString(hand) + " drawn " + drawn);
			}
		}
		
		//System.out.println("hand " + Arrays.toString(maxh));
		return maxh;
	}
}
