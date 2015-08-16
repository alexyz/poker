
package exp;

import java.util.*;

import pet.eq.*;

/**
 * experimental draw poker functions
 */
public class DrawPoker2 {
	
	public static void main(String[] args) {
		
		// [4h 2d 5d 7h 3s] draw 1 blockers 7h
		String[] x = new String[] { "4h", "2d", "5d", "7h", "3s" };
		List<Draw> l = new ArrayList<>();
		
		DrawPrediction.getDrawingHand(l, x, 1, false, null);
		Collections.sort(l);
		Collections.reverse(l);
		for (Draw d : l) {
			System.out.println("  " + d);
		}
		
		l.clear();
		DrawPrediction.getDrawingHand(l, x, 1, false, new String[] { "7h" });
		Collections.sort(l);
		Collections.reverse(l);
		for (Draw d : l) {
			System.out.println("  " + d);
		}
		
		l.clear();
		DrawPrediction.getDrawingHand(l, x, 1, false, new String[] { "7h", "5d" });
		Collections.sort(l);
		Collections.reverse(l);
		for (Draw d : l) {
			System.out.println("  " + d);
		}
		
		//String[] x = new String[] { "8h", "7h", "5s", "4c", "3s" };
		//String[] x = new String[] { "5h", "4c", "3s", "2d", "Ts" };
		//String[] x = new String[] { "7h", "6s", "5d", "3s", "2s" };
		//String[] x = new String[] { "7s", "6h", "5s", "4h", "3s" };
		//String[] x = new String[] { "Kd", "Ks", "Qh", "Jc", "Tc" };
		/*
		String[] x = new String[] { "As", "Ac", "Tc", "5c", "2c" };
		System.out.println("==" + PokerUtil.cardsString(x) + "==");
		List<DrawPoker.Draw> l = new ArrayList<DrawPoker.Draw>();
		for (int n = 1; n < 5; n++) {
			String[] y = DrawPoker.getDrawingHand(l, x, n, true, null);
		}
		Collections.sort(l);
		Collections.reverse(l);
		for (DrawPoker.Draw d : l) {
			System.out.println("  " + d);
		}
		*/
		
		/*
		int[] a = Poker.highValues();
		for (int n = 0; n < a.length; n++) {
			System.out.println(String.format("%.2f %03d -> %s", ((1f * n) / a.length), n, Poker.valueString(a[n])));
		}
		*/
		
		/*
		for (float f = 0; f <= 1f; f += (1f / 128)) {
			int i = (int) ((a.length - 1) * f);
			System.out.println("f " + f + " hand " + Poker.valueString(a[i]));
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
		
		//String[] x = new String[] { "2c", "5d", "4h", "8d", "4c" };

		/*
		List<Draw> l = new ArrayList<Draw>();
		for (int n = 0; n <= 5; n++) {
			String[] y = getDrawingHand(l, x, n, Value.dsLowValue, 2f);
			String[] z = DrawPoker.getDraw(x, n);
			System.out.println("draw " + n + " old: " + Arrays.toString(z));
			System.out.println("       new: " + Arrays.toString(y));
		}
		
		Collections.sort(l);
		Collections.reverse(l);
		for (Draw d : l) {
			System.out.println(String.format("%.6f -> %s", d.score, Arrays.toString(d.hole)));
		}
		*/
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
	
	
	
	/*
	public static String[] getDrawingHand(final String[] hand, final int drawn, boolean hi) {
		return getDrawingHand(null, hand, drawn, hi ? Value.hiValue : Value.dsLowValue, 3f);
	}
	*/
	
}
