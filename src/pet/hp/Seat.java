package pet.hp;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A player sitting at a table (never sitting out).
 * This object should be considered immutable.
 */
public class Seat implements Serializable {
	/** seat number */
	public byte num;
	/** player name */
	public String name;
	/** starting chips */
	public int chips;
	/** seats final hole cards */
	public String[] holeCards;
	/** amount won */
	public int won;
	/**
	 * amount put in pot, equal to sum of amount of player actions in hand minus
	 * the uncalled amount for that player (i.e. if they had everyone covered)
	 */
	public int pip;
	/**
	 * number of cards discarded for each draw - this could be in the action but
	 * it's not very practical there as you can only draw once per street anyway
	 */
	public byte drawn0, drawn1, drawn2;
	/** seats hand reached show down */
	public boolean showdown;
	/** seat posted a big blind */
	public boolean bigblind;
	/** seat posted a small blind */
	public boolean smallblind;
	
	/** return number drawn indexed from 0 */
	public int drawn(int draw) {
		switch (draw) {
			case 0: return drawn0;
			case 1: return drawn1;
			case 2: return drawn2;
			default: throw new RuntimeException("invalid draw " + draw);
		}
	}
	
	public void setDrawn(int draw, byte drawn) {
		switch (draw) {
			case 0: drawn0 = drawn; return;
			case 1: drawn1 = drawn; return;
			case 2: drawn2 = drawn; return;
			default: throw new RuntimeException("invalid draw " + draw);
		}
	}
	
	@Override
	public String toString() {
		String s = num + ":" + name + "(" + chips + ")";
		if (holeCards != null) {
			s += " " + Arrays.asList(holeCards);
		}
		if (drawn0 > 0) {
			s += " discards " + drawn0;
			// bit of a hack as we don't know game type here
			if (drawn1 + drawn2 > 0) {
				s += ", " + drawn1 + ", " + drawn2;
			}
		}
		return s;
	}
	
}
