
package pet.eq;

import java.util.*;

public class DrawPoker2 {
	
	private static int[] uniqueValues;
	
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
		String[] x = new String[] { "Kd", "Ks", "Qh", "Jc", "Tc" };
		
		List<Draw> l = new ArrayList<Draw>();
		
		for (int n = 0; n <= 5; n++) {
			String[] y = getDrawingHand(l, x, n, 4f);
			String[] z = DrawPoker.getDraw(x, n);
			System.out.println("draw " + n + " old: " + Arrays.toString(z));
			System.out.println("       new: " + Arrays.toString(y));
		}
		
		Collections.sort(l);
		Collections.reverse(l);
		for (Draw d : l) {
			System.out.println(String.format("%.6f -> %s", d.value, Arrays.toString(d.hole)));
		}
		
	}
	
	/**
	 * Go through every possible 5 card hand and collect the unique hand values in order
	 */
	private static int[] getUniqueValues() {
		if (uniqueValues != null) {
			return uniqueValues;
		}
		
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
		
		int[] a = new int[uniqueValueSet.size()];
		int i = 0;
		for (int v : uniqueValueSet) {
			a[i++] = v;
		}
		Arrays.sort(a);
		uniqueValues = a;
		return a;
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
	
	public static class Draw implements Comparable<Draw> {
		public final String[] hole;
		public float value;
		public Draw(String[] hole, float value) {
			this.hole = hole;
			this.value = value;
		}
		@Override
		public int compareTo(Draw other) {
			return (int) Math.signum(value - other.value);
		}
	}
	
	private static float getScore(int v, float bigPlay) {
		int[] uniqueValues = getUniqueValues();
		final int p = Arrays.binarySearch(uniqueValues, v);
		if (p < 0) {
			throw new RuntimeException();
		}
		return (float) Math.pow((1f * p) / (uniqueValues.length - 1f), bigPlay);
	}

	public static String[] getDrawingHand(List<Draw> draws, final String[] hand, final int drawn, float bigPlay) {
		if (drawn < 0 || drawn > 5) {
			throw new RuntimeException();
		}
		
		if (drawn == 5) {
			return new String[0];
		}
		
		if (drawn == 0) {
			if (draws != null) {
				draws.add(new Draw(hand, getScore(Poker.value(hand), bigPlay)));
			}
			return hand.clone();
		}
		
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
				int value = Poker.value(drawnHand);
				score += getScore(value, bigPlay);
			}
			
			float averageScore = score / (1.0f * jmax);
			String[] drawingHand = Arrays.copyOf(drawnHand, 5 - drawn);
			//System.out.println("hand: " + Arrays.toString(drawingHand) + " score: " + score + " mean value: " + meanval);
			if (draws != null) {
				draws.add(new Draw(drawingHand, averageScore));
			}
					
			if (score > maxScore) {
				// copy new max hole cards
				maxDrawingHand = drawingHand;
				maxScore = score;
			}
		}
		
		//System.out.println("hand " + Arrays.toString(maxh2));
		return maxDrawingHand;
	}
	
}
