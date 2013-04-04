package pet.eq;

import java.awt.Color;

/**
 * General utility methods
 */
public class PokerUtil {
	
	public static String cardString(String card) {
		return new String(new char[] { Poker.face(card), PokerUtil.suitSymbol(card, false) });
	}
	
	/**
	 * convert hand to unicode symbols
	 */
	public static String cardsString(String[] cards) {
		if (cards != null) {
			StringBuilder sb = new StringBuilder(cards.length * 2);
			for (String c : cards) {
				if (c != null) {
					sb.append(c.charAt(0)).append(PokerUtil.suitSymbol(c, false));
				} else {
					sb.append("--");
				}
			}
			return sb.toString();
		} else {
			return "";
		}
	}
	
	/**
	 * return integer value of cards (must be sorted, and no longer than 5)
	 */
	public static int cardsValue(String[] cards) {
		if (cards.length > 5) {
			throw new RuntimeException("too many cards");
		}
		int v = 0;
		for (String card : cards) {
			v *= 16;
			v += Poker.faceValueAH(card);
		}
		for (String card : cards) {
			v *= 4;
			v += suitValue(card);
		}
		return v;
	}
	
	private static final Color C_COL = new Color(0, 128, 0);
	private static final Color D_COL = new Color(0, 0, 128);
	
	/**
	 * Return colour of suit
	 */
	public static Color suitColour (char s) {
		// switch instead of map due to primitive type
		// TODO could just use "schd".indexOf(suit)
		switch (s) {
			case Poker.S_SUIT: return Color.black;
			case Poker.C_SUIT: return C_COL;
			case Poker.H_SUIT: return Color.red;
			case Poker.D_SUIT: return D_COL;
			default:
				throw new RuntimeException("unknown suit: " + s);
		}
	}
	
	/**
	 * Get the unicode suit symbol
	 */
	public static char suitSymbol(String c, boolean heavy) {
		switch (Poker.suit(c)) {
			//case Poker.C_SUIT: return heavy ? '\u2663' : '\u2667';
			//case Poker.D_SUIT: return heavy ? '\u2666' : '\u2662';
			//case Poker.H_SUIT: return heavy ? '\u2665' : '\u2661';
			//case Poker.S_SUIT: return heavy ? '\u2660' : '\u2664';
			case Poker.C_SUIT: return '\u2663';
			case Poker.D_SUIT: return heavy ? '\u2666' : '\u2662';
			case Poker.H_SUIT: return heavy ? '\u2665' : '\u2661';
			case Poker.S_SUIT: return '\u2660';
			default: return 0;
		}
	}
	
	/**
	 * Return suit value from 0-3 (for sorting purposes)
	 */
	public static int suitValue(String card) {
		switch (Poker.suit(card)) {
			case Poker.C_SUIT: return 3;
			case Poker.D_SUIT: return 2;
			case Poker.H_SUIT: return 1;
			case Poker.S_SUIT: return 0;
			default: return -1;
		}
	}
	
}
