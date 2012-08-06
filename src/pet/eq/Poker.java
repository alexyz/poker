package pet.eq;

import java.util.*;

/**
 * Poker hand valuation.
 */
public abstract class Poker {
	
	/* 
	 * poker hand values are represented by 7 x 4 bit values (28 bits total):
	 * 0x7654321
	 * 7 - hand type (high, a-5 low, 2-7 low)
	 * 6 - rank
	 * 5 - most significant card (if any)
	 * ...
	 * 1 - least significant card
	 */
	
	/** hand value type mask */
	protected static final int TYPE = 0xf000000;
	/** high hand value type */
	protected static final int HI_TYPE = 0;
	/** deuce to seven low hand value type */
	protected static final int DS_LOW_TYPE = 0x1000000;
	/** ace to five low hand value type */
	protected static final int AF_LOW_TYPE = 0x2000000;
	
	/** rank mask (allowing 20 bits for hand value, i.e. 4 bits per card) */
	protected static final int RANK = 0xf00000;
	/** rank and hand value mask */
	protected static final int HAND = 0xffffff;
	
	/** high card bit mask (always zero) */
	protected static final int H_MASK = 0;
	/** pair rank bit mask */
	protected static final int P_MASK = 0x100000;
	/** two pair rank bit mask */
	protected static final int TP_MASK = 0x200000;
	/** three of a kind rank bit mask */
	protected static final int TK_MASK = 0x300000;
	/** straight bit mask */
	protected static final int ST_MASK = 0x400000;
	/** flush bit mask */
	protected static final int FL_MASK = 0x500000;
	/** full house bit mask */
	protected static final int FH_MASK = 0x600000;
	/** four of a kind bit mask */
	protected static final int FK_MASK = 0x700000;
	/** straight flush rank mask */
	protected static final int SF_MASK = 0x800000;
	/** impossible rank higher than straight flush */
	protected static final int INV_MASK = 0x900000;
	
	/** number of high ranks */
	public static final int RANKS = 9;
	
	/**
	 * short rank names (value >> 20)
	 */
	public static final String[] ranknames = { "Hc", "P", "2P", "3K", "St", "Fl", "FH", "4K", "SF", "L" };
	/** card suit representations */
	public static final char H_SUIT = 'h', C_SUIT = 'c', S_SUIT = 's', D_SUIT = 'd';
	/** complete deck in face then suit order, lowest first */
	// TODO other arrays should be immutable
	public static final List<String> deck = Collections.unmodifiableList(Arrays.asList(
			"2h", "2s", "2c", "2d",
			"3h", "3s", "3c", "3d", "4h", "4s", "4c", "4d", "5h", "5s", "5c",
			"5d", "6h", "6s", "6c", "6d", "7h", "7s", "7c", "7d", "8h", "8s",
			"8c", "8d", "9h", "9s", "9c", "9d", "Th", "Ts", "Tc", "Td", "Jh",
			"Js", "Jc", "Jd", "Qh", "Qs", "Qc", "Qd", "Kh", "Ks", "Kc", "Kd",
			"Ah", "As", "Ac", "Ad" 
			));
	
	/** complete suits */
	public static final char[] suits = { S_SUIT, H_SUIT, C_SUIT, D_SUIT };
	
	/** array of all possible unique hi hand values (there are only approx 7500) */
	private static int[] uniqueValues;
	
	/**
	 * calculates high value of hand
	 */
	public static final Value hiValue = new Value(Equity.HI_ONLY) {
		@Override
		public final int value(String[] hand) {
			return Poker.value(hand);
		}
		@Override
		public float score(String[] hand, float bias) {
			return DrawPoker2.score(value(hand), bias, true);
		}
	};
	
	/**
	 * Calculates ace to five low value of hand
	 */
	public static final Value afLowValue = new Value(Equity.AFLO_ONLY) {
		@Override
		public final int value(String[] hand) {
			return Poker.lowValue(hand);
		}
		@Override
		public float score(String[] hand, float bias) {
			throw new RuntimeException("not yet implemented");
		}
	};
	
	/**
	 * deuce to seven low value function
	 */
	protected static final Value dsLowValue = new Value(Equity.DSLO_ONLY) {
		@Override
		public int value(String[] hand) {
			return dsValue(hand);
		}
		@Override
		public float score(String[] hand, float bias) {
			return DrawPoker2.score(Poker.value(hand), bias, false);
		}
	};
	
	/**
	 * count low cards
	 */
	static int lowCount(String[] hand, boolean acehigh) {
		int count = 0;
		for (int n = 0; n < hand.length; n++) {
			if (faceValue(hand[n], acehigh) <= 8) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * get ace to five low value of hand.
	 * returns 0 if no low.
	 * FIXME this isn't proper low, as low can have pairs (though not str/fl)
	 */
	static int lowValue(String[] hand) {
		validate(hand);
		if (lowCount(hand, false) ==  5) {
			int p = isPair(hand, false);
			if (p < P_MASK) {
				// no pairs
				// invert value
				int v = AF_LOW_TYPE | (INV_MASK - p);
				return v;
			}
		}
		return 0;
	}
	
	/** check hand is 5 cards and non of the cards are duplicated */
	private static void validate(String[] h) {
		if (h.length != 5) {
			throw new RuntimeException("invalid hand length: " + Arrays.toString(h));
		}
		for (int n = 0; n < h.length; n++) {
			String c = h[n];
			if ("23456789TJQKA".indexOf(face(c)) == -1 || "hdsc".indexOf(suit(c)) == -1) {
				throw new RuntimeException("invalid hand " + Arrays.toString(h));
			}
			// check for dupe
			for (int m = n + 1; m < h.length; m++) {
				if (c.equals(h[m])) {
					throw new RuntimeException("invalid hand " + Arrays.toString(h));
				}
			}
		}
	}
	
	/**
	 * Get high value of 5 card hand
	 */
	static int value(String[] hand) {
		validate(hand);
		int p = isPair(hand, true);
		if (p < P_MASK) {
			boolean f = isFlush(hand);
			int s = isStraight(hand);
			if (f) {
				if (s > 0) {
					return SF_MASK | s;
				} else {
					return FL_MASK | p;
				}
			} else if (s > 0) {
				return ST_MASK | s;
			}
		}
		return p;
	}
	
	/**
	 * return true if flush
	 */
	private static boolean isFlush(String[] hand) {
		char s = suit(hand[0]);
		for (int n = 1; n < 5; n++) {
			if (suit(hand[n]) != s) {
				return false;
			}
		}
		return true;
	}
	
	/** 
	 * return value of high card of straight or 0 
	 */
	private static int isStraight(String[] hand) {
		int x = 0;
		// straight value
		int str = 5;
		for (int n = 0; n < hand.length; n++) {
			// sub 1 so bottom bit equals ace low
			int v = faceValue(hand[n], true) - 1;
			x |= (1 << v);
			if (v == 13) {
				// add ace low as well as ace high
				x |= 1;
			}
		}
		// [11111000000001]
		while (x >= 31) {
			if ((x & 31) == 31) {
				return str;
			}
			x >>= 1;
			str++;
		}
		return 0;
	}
	
	/**
	 * Return pair value or high cards.
	 * Does not require sorted hand
	 */
	private static int isPair(String[] hand, boolean acehigh) {
		// count card face frequencies (3 bits each) -- 0, 1, 2, 3, 4
		long v = 0;
		for (int n = 0; n < hand.length; n++) {
			v += (1L << ((14 - faceValue(hand[n], acehigh)) * 3));
		}
		// get the card faces for each frequency
		int fk = 0, tk = 0, pa = 0, hc = 0;
		for (int f = 14; v != 0; v >>= 3, f--) {
			int i = (int) (v & 7);
			if (i == 0) {
				continue;
			} else if (i == 1) {
				hc = (hc << 4) | f;
			} else if (i == 2) {
				pa = (pa << 4) | f;
			} else if (i == 3) {
				tk = f;
			} else if (i == 4) {
				fk = f;
			}
		}
		
		if (fk != 0) {
			return FK_MASK | (fk << 4) | hc;
		} else if (tk != 0) {
			if (pa != 0) {
				return FH_MASK | (tk << 4) | pa;
			} else {
				return TK_MASK | (tk << 8) | hc;
			}
		} else if (pa >= 16) {
			return TP_MASK | (pa << 4) | hc;
		} else if (pa != 0) {
			return P_MASK | (pa << 12) | hc;
		} else {
			return H_MASK | hc;
		}
	}
	
	/**
	 * deuce to seven value - exact opposite of high value
	 */
	static int dsValue(String[] hand) {
		return Poker.DS_LOW_TYPE | (Poker.INV_MASK - Poker.value(hand));
	}
	
	/**
	 * Return integer value of card face, ace high or low (from A = 14 to 2 = 2 or K = 13 to A = 1)
	 */
	static int faceValue(String card, boolean acehigh) {
		if (acehigh) {
			int i = "23456789TJQKA".indexOf(face(card));
			if (i >= 0) {
				return i + 2;
			}
		} else {
			int i = "A23456789TJQK".indexOf(face(card));
			if (i >= 0) {
				return i + 1;
			}
		}
		throw new RuntimeException("unknown face " + card);
	}
	
	/**
	 * Returns lower case character representing suit, i.e. s, d, h or c
	 */
	public static char suit(String card) {
		return card.charAt(1);
	}
	
	/**
	 * Return character symbol of face value
	 */
	private static char valueFace(int x) {
		int v = x & 0xf;
		// allow 0 index and ace low
		return "?A23456789TJQKA".charAt(v);
	}
	
	/**
	 * Return string representation of hand value
	 */
	public static String valueString(int value) {
		if (value <= 0) {
			return "nil";
		}
		
		boolean hi;
		String type;
		switch (value & TYPE) {
			case HI_TYPE:
				hi = true;
				type = null;
				break;
			case DS_LOW_TYPE:
				hi = false;
				type = "27Lo";
				value = INV_MASK - (value & HAND);
				break;
			case AF_LOW_TYPE:
				hi = false;
				type = "A5Lo";
				value = INV_MASK - (value & HAND);
				break;
			default:
				throw new RuntimeException();
		}
		
		final char c1 = valueFace(value);
		final char c2 = valueFace(value >> 4);
		final char c3 = valueFace(value >> 8);
		final char c4 = valueFace(value >> 12);
		final char c5 = valueFace(value >> 16);
		
		String s;
		switch (value & 0xf00000) {
			case SF_MASK: s = "Straight Flush - " + c1 + " high"; break;
			case FK_MASK: s = "Four of a Kind " + c2 + " - " + c1; break;
			case FH_MASK: s = "Full House " + c2 + " full of " + c1; break;
			case FL_MASK: s = "Flush - " + c5 + " " + c4 + " " + c3 + " " + c2 + " " + c1; break;
			case ST_MASK: s = "Straight - " + c1 + " high"; break;
			case TK_MASK: s = "Three of a Kind " + c3 + " - " + c2 + " " + c1; break;
			case TP_MASK: s = "Two Pair " + c3 + " and " + c2 + " - " + c1; break;
			case P_MASK: s = "Pair " + c4 + " - " + c3 + " " + c2 + " " + c1; break;
			case H_MASK: s = c5 + " " + c4 + " " + c3 + " " + c2 + " " + c1 + (hi ? " high" : " low"); break;
			default: s = "Unknown";
		}
		
		return hi ? s : type + ": " + s;
	}
	
	public static char face(String card) {
		return card.charAt(0);
	}
	
	/** return rank of hand, from 0 to 9 (NOT the rank bitmask constants) */
	public static int rank(int value) {
		return value >> 20;
	}
	
	/**
	 * return the remaining cards in the deck.
	 * always returns new array
	 */
	public static String[] remdeck(String[][] aa, String[]... a) {
		ArrayList<String> list = new ArrayList<String>(deck);
		if (aa != null) {
			for (String[] x : aa) {
				rem1(list, x);
			}
		}
		if (a != null) {
			for (String[] x : a) {
				rem1(list, x);
			}
		}
		return list.toArray(new String[list.size()]);
	}
	
	private static void rem1(List<String> list, String[] a) {
		if (a != null) {
			for (String s : a) {
				if (!list.remove(s)) {
					throw new RuntimeException();
				}
			}
		}
	}
	
	/**
	 * Go through every possible 5 card hand and collect the unique hand values in order
	 */
	static int[] uniqueValues() {
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
	
	//
	// instance methods
	//
	
	/**
	 * Calculate equity for given board and hands.
	 */
	public abstract MEquity[] equity(String[] board, String[][] holes, String[] blockers);
	
	/**
	 * Calculate value of exact hi hand.
	 */
	public abstract int value(String[] board, String[] hole);
	
}
