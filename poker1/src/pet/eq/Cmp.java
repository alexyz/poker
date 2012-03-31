package pet.eq;

import java.util.Comparator;

public class Cmp {
	/**
	 * compare by face then suit
	 */
	public static final Comparator<String> cardCmp = new CardCmp(true);
	public static final Comparator<String> revCardCmp = new CardCmp(false);
	public static final Comparator<String> faceCmp = new FaceCmp();
}

class CardCmp implements Comparator<String> {
	
	private final int polarity;
	public CardCmp(boolean asc) {
		polarity = asc ? 1 : -1;
	}
	
	@Override
	public int compare(String c1, String c2) {
		int v = Poker.faceValue(c1) - Poker.faceValue(c2);
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
		return Poker.faceValue(c2) - Poker.faceValue(c1);
	}
	
}

