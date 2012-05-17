package pet.hp.info;

import java.util.Arrays;

import pet.eq.*;

/**
 * comparable wrapper for hole cards
 */
public class Hole implements Comparable<Hole> {
	private final String strValue;
	private final int intValue;
	public Hole(String[] hand) {
		// sort the hand, but don't modify
		hand = hand.clone();
		Arrays.sort(hand, Cmp.revCardCmp);
		strValue = PokerUtil.cardsString(hand);
		intValue = PokerUtil.cardsValue(hand);
	}
	@Override
	public int compareTo(Hole h) {
		return intValue - h.intValue;
	}
	@Override
	public boolean equals(Object o) {
		return o instanceof Hole && ((Hole)o).intValue == intValue;
	}
	@Override
	public int hashCode() {
		return intValue;
	}
	@Override
	public String toString() {
		return strValue;
	}
}
