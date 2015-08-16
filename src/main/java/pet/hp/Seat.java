package pet.hp;

import java.io.Serializable;
import java.util.Arrays;

import pet.eq.ArrayUtil;

/**
 * A player sitting at a table (never sitting out).
 * This object should be considered immutable.
 */
public class Seat implements Serializable {
	// There are a lot of instances of this object so use byte/short instead of
	// int where possible
	/** seat number */
	public byte num;
	/** player name */
	public String name;
	/** starting chips */
	public int chips;
	/** seats final down hole cards (not the up cards!) */
	public String[] downCards;
	/** seats final public cards */
	public String[] upCards;
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
	/** seats hand reached show down. implies hand.showdown is true */
	public boolean showdown;
	/** seat posted a big blind (could be in position other than bb) */
	public boolean bigblind;
	/** seat posted a small blind */
	public boolean smallblind;
	
	/** get seats final cards (both down and up) */
	public String[] cards() {
		if (downCards != null) {
			if (upCards != null) {
				return ArrayUtil.join(downCards, upCards);
			} else {
				return downCards;
			}
		} else {
			return null;
		}
	}
	
	/** return number drawn indexed from 0 */
	public int drawn(int draw) {
		switch (draw) {
			case 0: return drawn0;
			case 1: return drawn1;
			case 2: return drawn2;
			default: throw new RuntimeException("invalid draw " + draw);
		}
	}
	
	/**
	 * set number of cards drawn, index from 0
	 */
	public void setDrawn(int draw, int drawn) {
		switch (draw) {
			case 0: drawn0 = (byte) drawn; return;
			case 1: drawn1 = (byte) drawn; return;
			case 2: drawn2 = (byte) drawn; return;
			default: throw new RuntimeException("invalid draw " + draw);
		}
	}
	
	@Override
	public String toString() {
		String s = num + ":" + name + "(" + chips + ")";
		if (downCards != null) {
			s += " " + Arrays.asList(downCards);
		}
		if (upCards != null) {
			s += " " + Arrays.asList(upCards);
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
