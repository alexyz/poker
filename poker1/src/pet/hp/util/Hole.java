package pet.hp.util;

import java.util.Arrays;

import pet.eq.*;

/**
 * comparable wrapper for hole cards
 */
public class Hole implements Comparable<Hole> {
	private String strValue;
	private int intValue;
	public Hole(String[] hand) {
		hand = hand.clone();
		Arrays.sort(hand, Cmp.revCardCmp);
		strValue = PokerUtil.cardsString(hand);
		intValue = PokerUtil.cardsValue(hand);
	}
	@Override
	public int compareTo(Hole h) {
		System.out.println("comparing hole");
		return intValue - h.intValue;
	}
	@Override
	public String toString() {
		return strValue;
	}
}
