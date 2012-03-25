package pet.eq;

import java.awt.Color;
import java.util.Comparator;

/**
 * Poker hand analysis.
 */
public class Poker {

	/**
	 * Rank masks (allowing 20 bits for hand value, i.e. 4 bits per card)
	 */
	private static final int H_RANK = 0;
	private static final int P_RANK = 1 << 20;
	private static final int TP_RANK = 2 << 20;
	private static final int TK_RANK = 3 << 20;
	private static final int ST_RANK = 4 << 20;
	private static final int FL_RANK = 5 << 20;
	private static final int FH_RANK = 6 << 20;
	private static final int FK_RANK = 7 << 20;
	private static final int SF_RANK = 8 << 20;
	private static final int LOW_RANK = 9 << 20;
	/**
	 * short rank names (value >> 20)
	 */
	public static final String[] ranknames = { "H", "P", "2P", "3K", "S", "F", "FH", "4K", "SF" };
	/** card suit representations */
	public static final char H_SUIT = 'h', C_SUIT = 'c', S_SUIT = 's', D_SUIT = 'd';
	/** complete deck */
	public static final String[] FULL_DECK = new String[] { 
		"2h", "2s", "2c", "2d",
		"3h", "3s", "3c", "3d", "4h", "4s", "4c", "4d", "5h", "5s", "5c",
		"5d", "6h", "6s", "6c", "6d", "7h", "7s", "7c", "7d", "8h", "8s",
		"8c", "8d", "9h", "9s", "9c", "9d", "Th", "Ts", "Tc", "Td", "Jh",
		"Js", "Jc", "Jd", "Qh", "Qs", "Qc", "Qd", "Kh", "Ks", "Kc", "Kd",
		"Ah", "As", "Ac", "Ad" 
	};
	/** complete suits */
	public static final char[] suits = { S_SUIT, H_SUIT, C_SUIT, D_SUIT };
	private static final int[] facevals = mkfacevals();
	/**
	 * compare by face then suit
	 */
	public static Comparator<String> cmp = new Comparator<String>() {
		@Override
		public int compare(String c1, String c2) {
			int v = faceval(c1) - faceval(c2);
			if (v == 0) {
				v = suit(c1) - suit(c2);
			}
			return v;
		}
	};

	private static int[] mkfacevals() {
		// use array instead of map due to primitive type
		int[] fv = new int['T' - '2' + 1];
		fv['A' - '2'] = 14;
		fv['K' - '2'] = 13;
		fv['Q' - '2'] = 12;
		fv['J' - '2'] = 11;
		fv['T' - '2'] = 10;
		fv['9' - '2'] = 9;
		fv['8' - '2'] = 8;
		fv['7' - '2'] = 7;
		fv['6' - '2'] = 6;
		fv['5' - '2'] = 5;
		fv['4' - '2'] = 4;
		fv['3' - '2'] = 3;
		fv['2' - '2'] = 2;
		return fv;
	}

	/**
	 * Sort the hand in place (Arrays.sort copies array).
	 */
	private static void bubblesort(String[] h) {
		int l = h.length;
		while (l > 0) {
			int newl = 0;
			for (int n = 1; n < l; n++) {
				String x = h[n - 1];
				String y = h[n];
				if (faceval(y) - faceval(x) > 0) {
					newl = n;
					h[n - 1] = y;
					h[n] = x;
				}
			}
			l = newl;
		}
	}

	private final String[] hand = new String[5];
	public boolean debug;

	protected void println(String s) {
		if (debug) {
			System.out.println(s);
		}
	}

	private void copy(Object[] from, Object[] to) {
		for (int n = 0; n < from.length; n++) {
			to[n] = from[n];
		}
	}

	public boolean islow(String[] hand) {
		for (int n = 0; n < hand.length; n++) {
			if (faceval(hand[n], false) > 8) {
				return false;
			}
		}
		return true;
	}

	public int lowvalue(String[] hand) {
		if (islow(hand)) {
			int p = ispair(hand, false);
			if ((p & 0xf00000) == H_RANK) {
				// no pairs
				// invert value
				return (LOW_RANK | 0xfffff) - (p & 0xfffff);
			}
		}
		return 0;
	}

	/**
	 * Get high value of 5 card hand
	 */
	public int value(String[] hand_) {
		// copy so we can sort
		copy(hand_, hand);
		bubblesort(hand);
		int f = isflush(hand);
		int s = isstraight(hand);
		if (f != 0) {
			if (s != 0) {
				return SF_RANK | (s & 0xfffff);
			} else {
				return f;
			}
		} else if (s != 0) {
			return s;
		}
		return ispair(hand, true);
	}

	private static int isflush(String[] hand) {
		char s = suit(hand[0]);
		for (int n = 1; n < 5; n++) {
			if (suit(hand[n]) != s) {
				return 0;
			}
		}
		// requires sorted hand
		return FL_RANK | (faceval(hand[0]) << 16) + (faceval(hand[1]) << 12) + (faceval(hand[2]) << 8) + (faceval(hand[3]) << 4) + faceval(hand[4]);
	}

	private static int isstraight(String[] hand) {
		// requires sorted hand
		// max str is AKJQT
		// min str is A5432
		int v0 = faceval(hand[0]);
		int v = faceval(hand[1]);
		int hc = 0;
		if (v0 == 14 && v == 5) {
			hc = 5;
		} else if (v0 == v + 1) {
			hc = v0;
		}
		if (hc != 0) {
			for (int n = 2; n < 5; n++) {
				int vn = faceval(hand[n]);
				if (v != vn + 1) {
					return 0;
				}
				v = vn;
			}
			return ST_RANK | hc;
		}
		return 0;
	}

	/**
	 * Return pair value or high cards.
	 * Does not require sorted hand
	 */
	private static int ispair(String[] hand, boolean acehigh) {
		// count card face frequencies (3 bits each) -- 0, 1, 2, 3, 4
		long v = 0;
		for (int n = 0; n < hand.length; n++) {
			v += (1L << ((14 - faceval(hand[n], acehigh)) * 3));
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
			return FK_RANK | (fk << 4) | hc;
		} else if (tk != 0) {
			if (pa != 0) {
				return FH_RANK | (tk << 4) | pa;
			} else {
				return TK_RANK | (tk << 8) | hc;
			}
		} else if (pa >= 16) {
			return TP_RANK | (pa << 4) | hc;
		} else if (pa != 0) {
			return P_RANK | (pa << 12) | hc;
		} else {
			return H_RANK | hc;
		}
	}

	/**
	 * Return integer value of card face, ace high (from A = 14 to 2 = 2)
	 */
	private static int faceval(String card) {
		return facevals[card.charAt(0) - '2'];
	}

	/**
	 * Return integer value of card face, ace high or low (from A = 14 to 2 = 2 or K = 13 to A = 1)
	 */
	private static int faceval(String card, boolean acehigh) {
		int v = facevals[card.charAt(0) - '2'];
		if (v == 14 && !acehigh) {
			v = 1;
		}
		return v;
	}

	/**
	 * Returns lowercase character representing suit, i.e. s, d, h or c
	 */
	public static final char suit(String card) {
		return card.charAt(1);
	}

	/**
	 * Return character symbol of face value
	 */
	private static char valface(int x) {
		return ".A23456789TJQKA".charAt(x & 0xf);
	}

	/**
	 * Return string representation of hand value
	 */
	public static String desc(int v) {
		if (v == 0) {
			return "nil";
		}
		if ((v & 0xf00000) == LOW_RANK) {
			v = (LOW_RANK | 0xfffff) - v;
		}
		char c1 = valface(v);
		char c2 = valface(v >> 4);
		char c3 = valface(v >> 8);
		char c4 = valface(v >> 12);
		char c5 = valface(v >> 16);
		switch (v & 0xf00000) {
		case LOW_RANK: return c1 + " " + c2 + " " + c3 + " " + c4 + " " + c5 + " high";
		case SF_RANK: return "Straight Flush " + c1;
		case FK_RANK: return "Four of a Kind " + c2 + " - " + c1;
		case FH_RANK: return "Full House " + c2 + " full of " + c1;
		case FL_RANK: return "Flush - " + c5 + " " + c4 + " " + c3 + " " + c2 + " " + c1 + " high";
		case ST_RANK: return "Straight - " + c1 + " high";
		case TK_RANK: return "Three of a Kind " + c3 + " - " + c2 + " " + c1;
		case TP_RANK: return "Two Pair " + c3 + " and " + c2 + " - " + c1;
		case P_RANK: return "Pair " + c4 + " - " + c3 + " " + c2 + " " + c1;
		case H_RANK: return c5 + " " + c4 + " " + c3 + " " + c2 + " " + c1 + " high";
		default: return "Unknown";
		}
	}

	/**
	 * Return colour of suit
	 */
	public static Color suitcol (char s) {
		// switch instead of map due to primitive type
		switch (s) {
		case Poker.S_SUIT: return Color.black;
		case Poker.C_SUIT: return Color.green;
		case Poker.H_SUIT: return Color.red;
		case Poker.D_SUIT: return Color.blue;
		}
		throw new RuntimeException();
	}

	public static char suitsym(String c) {
		/*
		 * 2660 => ♠ 2661 => ♡ 2662 => ♢ 2663 => ♣ 2664 => ♤ 2665 => ♥ 2666 => ♦ 2667 => ♧
		 */
		switch (suit(c)) {
		case C_SUIT: return '♣';
		case D_SUIT: return '♦';
		case H_SUIT: return '♥';
		case S_SUIT: return '♠';
		default: return 0;
		}
	}

	public static char suitsyml(String c) {
		/*
		 * 2660 => ♠ 2661 => ♡ 2662 => ♢ 2663 => ♣ 2664 => ♤ 2665 => ♥ 2666 => ♦ 2667 => ♧
		 */
		switch (suit(c)) {
		case C_SUIT: return '♧';
		case D_SUIT: return '♢';
		case H_SUIT: return '♡';
		case S_SUIT: return '♤';
		default: return 0;
		}
	}

	public static char face(String card) {
		return card.charAt(0);
	}

	public static String getCardString(String[] cards, boolean sort) {
		if (cards != null) {
			StringBuilder sb = new StringBuilder(cards.length * 2);
			// FIXME sort by card val then suit
			// should probably do this in handinfo
			for (String c : cards) {
				if (c != null) {
					sb.append(c.charAt(0)).append(Poker.suitsyml(c));
				} else {
					sb.append("..");
				}
			}
			return sb.toString();
		} else {
			return "";
		}
	}

}
