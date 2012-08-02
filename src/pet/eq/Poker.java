package pet.eq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Poker hand valuation.
 */
public abstract class Poker {
	
	/** any mask (allowing 20 bits for hand value, i.e. 4 bits per card) */
	protected static final int MASK = 0xf00000;
	/** high card bit mask (always zero) */
	protected static final int H_MASK = 0;
	/** pair rank bit mask */
	protected static final int P_MASK = 1 << 20;
	/** two pair rank bit mask */
	protected static final int TP_MASK = 2 << 20;
	/** three of a kind rank bit mask */
	protected static final int TK_MASK = 3 << 20;
	/** straight bit mask */
	protected static final int ST_MASK = 4 << 20;
	/** flush bit mask */
	protected static final int FL_MASK = 5 << 20;
	/** full house bit mask */
	protected static final int FH_MASK = 6 << 20;
	/** four of a kind bit mask */
	protected static final int FK_MASK = 7 << 20;
	/** straight flush rank mask */
	protected static final int SF_MASK = 8 << 20;
	/** ace-five low rank mask */
	protected static final int LOW_MASK = 9 << 20;
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
	
	/**
	 * calculates high value of hand
	 */
	public static final Value hi = new Value() {
		@Override
		public final int value(String[] hand) {
			return Poker.value(hand);
		}
	};
	
	/**
	 * Calculates low value of hand
	 */
	public static final Value lo = new Value() {
		@Override
		public final int value(String[] hand) {
			return Poker.lowValue(hand);
		}
	};

	/**
	 * count low cards
	 */
	public static int lowCount(String[] hand, boolean acehigh) {
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
	 */
	public static int lowValue(String[] hand) {
		validate(hand);
		if (lowCount(hand, false) ==  5) {
			int p = isPair(hand, false);
			if (p < P_MASK) {
				// no pairs
				// invert value
				int v = LOW_MASK | (P_MASK - p);
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
	public static int value(String[] hand) {
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
		if (value == 0) {
			return "nil";
		}
		if ((value & MASK) == LOW_MASK) {
			value = (LOW_MASK | 0xfffff) - value;
		}
		char c1 = valueFace(value);
		char c2 = valueFace(value >> 4);
		char c3 = valueFace(value >> 8);
		char c4 = valueFace(value >> 12);
		char c5 = valueFace(value >> 16);
		switch (value & 0xf00000) {
			case LOW_MASK: return c1 + " " + c2 + " " + c3 + " " + c4 + " " + c5 + " low";
			case SF_MASK: return "Straight Flush - " + c1 + " high";
			case FK_MASK: return "Four of a Kind " + c2 + " - " + c1;
			case FH_MASK: return "Full House " + c2 + " full of " + c1;
			case FL_MASK: return "Flush - " + c5 + " " + c4 + " " + c3 + " " + c2 + " " + c1;
			case ST_MASK: return "Straight - " + c1 + " high";
			case TK_MASK: return "Three of a Kind " + c3 + " - " + c2 + " " + c1;
			case TP_MASK: return "Two Pair " + c3 + " and " + c2 + " - " + c1;
			case P_MASK: return "Pair " + c4 + " - " + c3 + " " + c2 + " " + c1;
			case H_MASK: return c5 + " " + c4 + " " + c3 + " " + c2 + " " + c1 + " high";
			default: return "Unknown";
		}
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
	 * Calculate equity for given board and hands.
	 */
	public abstract MEquity[] equity(String[] board, String[][] holes, String[] blockers);
	
	/**
	 * Calculate value of exact hi hand.
	 */
	public abstract int value(String[] board, String[] hole);
	
}
