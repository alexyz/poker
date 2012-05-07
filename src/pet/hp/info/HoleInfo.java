package pet.hp.info;

import java.util.Arrays;

import pet.eq.*;

/**
 * comparable wrapper for hole cards
 */
public class HoleInfo implements Comparable<HoleInfo> {
	private final String strValue;
	private final int intValue;
	public HoleInfo(String[] hand) {
		// sort the hand, but don't modify
		hand = hand.clone();
		Arrays.sort(hand, Cmp.revCardCmp);
		strValue = PokerUtil.cardsString(hand);
		intValue = PokerUtil.cardsValue(hand);
	}
	@Override
	public int compareTo(HoleInfo h) {
		return intValue - h.intValue;
	}
	@Override
	public String toString() {
		return strValue;
	}
}
