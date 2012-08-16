package pet;

import java.util.Arrays;

import pet.eq.Poker;

public class CardUtil {
	
	/** convert cards to integer */
	public static int toInt(final String[] cards) {
		if (cards == null || cards.length == 0) {
			return 0;
		}
		if (cards.length > 5) {
			throw new RuntimeException("more than 5 cards: " + Arrays.toString(cards));
		}
		int x = 0;
		for (int n = 0; n < cards.length; n++) {
			int y = Arrays.binarySearch(Poker.deckArrS, cards[n]);
			if (y < 0) {
				throw new RuntimeException("invalid card: " + cards[n]);
			}
			x = (x << 6) | (y + 1);
		}
		return x;
	}
	
	public static int count(int x) {
		int n = 0;
		while (x != 0) {
			n++;
			x >>>= 6;
		}
		if (x > 5) {
			throw new RuntimeException("more than 5 cards: " + Integer.toHexString(x));
		}
		return n;
	}
	
	/** convert integer to cards */
	public static String[] toArray(int x) {
		if (x == 0) {
			return null;
		}
		final String[] cards = new String[count(x)];
		int n = cards.length - 1;
		while (x != 0) {
			cards[n--] = Poker.deckArrS[(x & 0x3f) - 1];
			x >>>= 6;
		}
		return cards;
	}
	
}
