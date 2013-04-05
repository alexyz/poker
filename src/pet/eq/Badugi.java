
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
	
	public static final String[] shortRankNames = { "B4", "B5", "B6", "B7", "B", "3", "2/1" };
	
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
		for (int n = 0; n < 10; n++) {
			Collections.shuffle(l);
			String[] a = l.subList(0, 4).toArray(new String[4]);
			System.out.println(Arrays.toString(a) + " => " + valueString(badugiValue(a)));
		}
	}
	
	/** get value of badugi hand */
	public static final int badugiValue (String[] hand) {
		if (hand.length != 4) {
			throw new RuntimeException();
		}
		// the values are bigger for worse hands, so invert
		return (B0_RANK - v4(hand)) | Poker.BADUGI_TYPE;
	}
	
	/** get value of 4 card hand */
	private static int v4 (String[] h) {
		int v = B0_RANK;
		for (int n = 0; n < V3.length; n++) {
			byte[] p = V3[n];
			int p0 = p[0], p1 = p[1], p2 = p[2], p3 = p[3];
			if (eq(h[p0], h[p1])) {
				// (at least) two cards are equal, can't be B4
				// try 3 card hand for each of the two cards
				v = Math.min(v, v3(h[p0], h[p2], h[p3]));
				v = Math.min(v, v3(h[p1], h[p2], h[p3]));
			}
		}
		if (v == B0_RANK) {
			// no cards are equal
			// its B4, sort...
			int[] a = { faceValueAL(h[0]), faceValueAL(h[1]), faceValueAL(h[2]), faceValueAL(h[3]) };
			ArrayUtil.sort(a);
			return B4_RANK | (a[3] << 12) | (a[2] << 8) | (a[1] << 4) | a[0];
		}
		return v;
	}

	/** get value of 3 card hand */
	private static int v3 (String c0, String c1, String c2) {
		int v = B0_RANK;
		for (int n = 0; n < V2.length; n++) {
			byte[] p = V2[n];
			String cp0 = arg(p[0], c0, c1, c2);
			String cp1 = arg(p[1], c0, c1, c2);
			if (eq(cp0, cp1)) {
				// (at least) two are equal
				// can't be B3
				// try 2 card hand for each of the two equal cards
				String cp2 = arg(p[2], c0, c1, c2);
				v = Math.min(v, v2(cp0, cp2));
				v = Math.min(v, v2(cp1, cp2));
			}
		}
		if (v == B0_RANK) {
			// it's a B3, sort
			int[] a = { faceValueAL(c0), faceValueAL(c1), faceValueAL(c2) };
			ArrayUtil.sort(a);
			v = B3_RANK | (a[2] << 8) | (a[1] << 4) | a[0];
		}
		return v;
	}

	/** get value of two card hand */
	private static int v2 (String c0, String c1) {
		if (eq(c0, c1)) {
			// oh dear, both equal, it's a B1
			return B1_RANK | faceValueAL(c0);
			
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

	/** return true of the two cards are equal */
	private static boolean eq (String c1, String c2) {
		return suit(c1) == suit(c2) || face(c1) == face(c2);
	}

	/**
	 * return indexed argument, a bit like an struct, i.e. a data structure that
	 * is not on the heap
	 */
	private static String arg (int i, String s0, String s1, String s2) {
		switch (i) {
			case 0:
				return s0;
			case 1:
				return s1;
			case 2:
				return s2;
			default:
				return null;
		}
	}
	
	/**
	 * return description of hand.
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
	
}
