package pet.eq;

import java.util.Comparator;

public class Cmp {
	/**
	 * compare by face (ace high) then suit, lowest first
	 */
	public static final Comparator<String> cardCmp = new CardCmp(true);
	/**
	 * compare by face (ace high) then suit, highest first
	 */
	public static final Comparator<String> revCardCmp = new CardCmp(false);
	/**
	 * compare by face (ace low) only, highest first
	 */
	public static final Comparator<String> faceCmpAL = new FaceCmp(true, false);
}

class CardCmp implements Comparator<String> {
	
	private final int polarity;
	public CardCmp(boolean asc) {
		polarity = asc ? 1 : -1;
	}
	
	@Override
	public int compare(String c1, String c2) {
		int v = Poker.faceValueAH(c1) - Poker.faceValueAH(c2);
		if (v == 0) {
			v = Poker.suit(c1) - Poker.suit(c2);
		}
		return polarity * v;
	}
}

class FaceCmp implements Comparator<String> {

	private final int polarity;
	private final boolean aceHigh;
	
	public FaceCmp(boolean asc, boolean aceHigh) {
		this.aceHigh = aceHigh;
		this.polarity = asc ? 1 : -1;
	}
	@Override
	public int compare(String c1, String c2) {
		// highest first
		return polarity * (Poker.faceValueAH(c2) - Poker.faceValueAH(c1));
	}
	
}
