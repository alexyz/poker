
package pet.eq;

import java.util.*;

import static pet.eq.Poker.*;

/**
 * methods for badugi valuation. note this doesn't subclass Poker, as badugi is
 * hand valuation only, the game is otherwise the game as draw poker
 */
public class Badugi {
	
	/*
	 * badugi hand values as represented by an integer: 
	 * 0x87654321 
	 * 8 = 0 
	 * 7 = 4 (BADUGI_TYPE) 
	 * 6 = rank (B1-B4) 
	 * 5 = 0 
	 * 4 = most significant (highest) card
	 * 3,2,1 = less significant cards
	 * 
	 * NOTE: badugi valuation is not very efficient, due to its use of multiple
	 * arrays. the best alternative is to pack the cards into an integer, but
	 * that is very fiddly and not really worth the effort at this time
	 */
	
	/** unachievable worst value */
	private static final int B0_RANK = 0x500000;
	/** 1 card hand */
	private static final int B1_RANK = 0x400000;
	/** 2 card hand */
	private static final int B2_RANK = 0x300000;
	/** 3 card hand */
	private static final int B3_RANK = 0x200000;
	/** badugi */
	private static final int B4_RANK = 0x100000;
	
	public static final String[] shortRankNames = {
			"B4", "B5", "B6", "B7", "B", "3", "2/1"
	};
	
	/** three combinations of two card hands (one of [0] or [1], plus [2]) */
	private static final byte[][] V2 = { 
		{ 0, 1, 2 }, 
		{ 0, 2, 1 }, 
		{ 1, 2, 0 } 
	};
	
	/**
	 * six combinations of 3 card hands (one of [0] or [1], plus [2] and [3]).
	 * i.e. the first two columns are [0123] pick 2, and the second two are the
	 * remaining indexes
	 */
	private static final byte[][] V3 = { 
		{ 0, 1, 2, 3 }, 
		{ 0, 2, 1, 3 }, 
		{ 0, 3, 1, 2 }, 
		{ 1, 2, 0, 3 },
		{ 1, 3, 0, 2 }, 
		{ 2, 3, 0, 1 } 
	};
	
	public static void main (String[] args) {
		ArrayList<String> l = new ArrayList<>(Poker.deck);
		Random r = new Random();
		for (int n = 0; n < 10; n++) {
			Collections.shuffle(l, r);
			String[] a = l.subList(0, 4).toArray(new String[4]);
			int d = r.nextInt(4) + 1;
			System.out.println(Arrays.toString(a) + " => " + valueString(badugiValue(a)) + " => draw " + d + " => " + Arrays.toString(draw(a, d)));
		}
	}
	
	/** get value of badugi hand */
	public static final int badugiValue (String[] hand) {
		if (hand.length != 4) {
			throw new RuntimeException("invalid badugi hand: " + Arrays.toString(hand));
		}
		// the values are bigger for worse hands, so invert
		return (B0_RANK - v4(hand)) | Poker.BADUGI_TYPE;
	}
	
	/** get value of 4 card hand */
	private static int v4 (String[] hand) {
		int v = B0_RANK;
		String[] h3 = new String[3];
		for (int n = 0; n < V3.length; n++) {
			byte[] p = V3[n];
			String hp0 = hand[p[0]], hp1 = hand[p[1]], hp2 = hand[p[2]], hp3 = hand[p[3]];
			if (eq(hp0, hp1)) {
				// (at least) two cards are equal, can't be B4
				// try 3 card hand for each of the two cards
				h3[0] = hp0;
				h3[1] = hp2;
				h3[2] = hp3;
				v = Math.min(v, v3(h3));
				h3[0] = hp1;
				v = Math.min(v, v3(h3));
			}
		}
		if (v == B0_RANK) {
			// no cards are equal
			// its B4, sort...
			int[] a = {
					faceValueAL(hand[0]), faceValueAL(hand[1]), faceValueAL(hand[2]), faceValueAL(hand[3])
			};
			ArrayUtil.sort(a);
			return B4_RANK | (a[3] << 12) | (a[2] << 8) | (a[1] << 4) | a[0];
		}
		return v;
	}
	
	/** get value of 3 card hand */
	private static int v3 (String[] h3) {
		int v = B0_RANK;
		for (int n = 0; n < V2.length; n++) {
			byte[] p = V2[n];
			String cp0 = h3[p[0]];
			String cp1 = h3[p[1]];
			if (eq(cp0, cp1)) {
				// (at least) two are equal
				// can't be B3
				// try 2 card hand for each of the two equal cards
				String cp2 = h3[p[2]];
				v = Math.min(v, v2(cp0, cp2));
				v = Math.min(v, v2(cp1, cp2));
			}
		}
		if (v == B0_RANK) {
			// it's a B3, sort
			int[] a = {
					faceValueAL(h3[0]), faceValueAL(h3[1]), faceValueAL(h3[2])
			};
			ArrayUtil.sort(a);
			v = B3_RANK | (a[2] << 8) | (a[1] << 4) | a[0];
		}
		return v;
	}
	
	/** get value of two card hand */
	private static int v2 (String c0, String c1) {
		if (eq(c0, c1)) {
			// oh dear, both equal, it's a B1
			return v1(c0);
			
		} else {
			// it's a B2
			int v0 = faceValueAL(c0);
			int v1 = faceValueAL(c1);
			if (v0 > v1) {
				int t = v0;
				v0 = v1;
				v1 = t;
			}
			return B2_RANK | (v1 << 4) | v0;
		}
	}
	
	/**
	 * return value of 1 card
	 */
	private static int v1 (String c0) {
		return B1_RANK | faceValueAL(c0);
	}

	/**
	 * return value of variable length hand
	 */
	private static int v (String[] hand) {
		switch (hand.length) {
			case 4:
				return v4(hand);
			case 3:
				return v3(hand);
			case 2:
				return v2(hand[0], hand[1]);
			case 1:
				return v1(hand[0]);
			default:
				throw new RuntimeException("invalid drawing hand: " + Arrays.toString(hand));
		}
	}
	
	/** return true if the two cards are equal */
	private static boolean eq (String c1, String c2) {
		return suit(c1) == suit(c2) || face(c1) == face(c2);
	}
	
	/**
	 * return description of hand (with type bits set)
	 */
	static final String valueString (int value) {
		int v2 = B0_RANK - (value & Poker.HAND);
		char c0 = valueFace(v2 & 0xf);
		char c1 = valueFace((v2 >> 4) & 0xf);
		char c2 = valueFace((v2 >> 8) & 0xf);
		char c3 = valueFace((v2 >> 12) & 0xf);
		switch (v2 & Poker.RANK) {
			case B1_RANK:
				return "1-Card: " + c0;
			case B2_RANK:
				return "2-Card: " + c1 + " " + c0;
			case B3_RANK:
				return "3-Card: " + c2 + " " + c1 + " " + c0;
			case B4_RANK:
				return "Badugi: " + c3 + " " + c2 + " " + c1 + " " + c0;
			default:
				throw new RuntimeException("v2=" + Integer.toHexString(v2));
		}
	}
	
	/**
	 * get the index into the short rank names array.
	 */
	public static int rank (int value) {
		// { "B4", "B5", "B6", "B7", "B", "3", "2/1" };
		int v2 = B0_RANK - (value & Poker.HAND);
		switch (v2 & Poker.RANK) {
			case B1_RANK:
			case B2_RANK:
				return 6;
			case B3_RANK:
				return 5;
			case B4_RANK: {
				// get most significant card
				int hc = (v2 & 0xf000) >> 12;
				// 4->0, 5->1, 6->2, 7->3, 8+->4
				return hc <= 7 ? hc - 4 : 4;
			}
			default:
				throw new RuntimeException("v2=" + Integer.toHexString(v2));
		}
	}
	
	/**
	 * get the likely drawing hand.
	 * should probably do something with blockers...
	 */
	public static String[] draw (String[] hand, int drawn) {
		switch (drawn) {
			case 0:
				return hand;
			case 1:
			case 2:
			case 3:
				return draw2(hand, 4 - drawn);
			case 4:
				return new String[0];
			default:
				throw new RuntimeException("invalid drawn: " + drawn);
		}
	}
	
	/** return the cards making up the best badugi hand, k = 1, 2 or 3 */
	private static String[] draw2 (String[] hand, int k) {
		final String[] h = new String[k];
		final int pmax = MathsUtil.binomialCoefficientFast(4, k);
		int vmin = B0_RANK;
		int vminp = 0;
		// find p for worst hand
		for (int p = 0; p < pmax; p++) {
			MathsUtil.kCombination(k, p, hand, h, 0);
			int v = v(h);
			if (v < vmin) {
				vmin = v;
				vminp = p;
			}
		}
		// get the cards for the winning combination
		MathsUtil.kCombination(k, vminp, hand, h, 0);
		return h;
	}
	
}
