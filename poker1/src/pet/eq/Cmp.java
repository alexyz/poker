package pet.eq;

import java.util.Comparator;

public class Cmp {
	/**
	 * compare by face then suit, lowest first
	 */
	public static final Comparator<String> cardCmp = new CardCmp(true);
	/**
	 * compare by face then suit, highest first
	 */
	public static final Comparator<String> revCardCmp = new CardCmp(false);
	/**
	 * compare by face only, highest first
	 */
	public static final Comparator<String> faceCmp = new FaceCmp();
}

class CardCmp implements Comparator<String> {
	
	private final int polarity;
	public CardCmp(boolean asc) {
		polarity = asc ? 1 : -1;
	}
	
	@Override
	public int compare(String c1, String c2) {
		int v = Poker.faceValue(c1, true) - Poker.faceValue(c2, true);
		if (v == 0) {
			v = Poker.suit(c1) - Poker.suit(c2);
		}
		return polarity * v;
	}
}

class FaceCmp implements Comparator<String> {

	@Override
	public int compare(String c1, String c2) {
		// highest first
		return Poker.faceValue(c2, true) - Poker.faceValue(c1, true);
	}
	
}

