package pet.eq;

import java.util.*;

/**
 * Represents the equity of a hand
 */
public class Equity {
	
	/**
	 * current value
	 */
	public int current;
	/**
	 * Currently winning and not tying
	 */
	public boolean curwin;
	/**
	 * currently tying
	 */
	public boolean curtie;
	/** percentage of hands won but not tied */
	public float won;
	/** percentage of hands tied but not won */
	public float tied;
	/** percentage of hands won or tied by rank (value >> 20) */
	public final float[] wonrank = new float[9];
	/** percentage that each card will make best hand */
	public final Map<String,Float> outs = new TreeMap<String,Float>();
	
	int woncount;
	int tiedcount;
	// FIXME hi only
	final int[] wonrankcount = new int[9];
	/** count that each card (as part of group of k cards) will make the best hand */
	final Map<String,int[]> outcount = new TreeMap<String,int[]>();

	/**
	 * update percentage won, tied and by rank
	 */
	void summariseEquity(int nb) {
		float total = woncount + tiedcount;
		won = (woncount * 100f) / nb;
		tied = (tiedcount * 100f) / nb;
		for (int n = 0; n < wonrankcount.length; n++) {
			wonrank[n] = total != 0 ? (wonrankcount[n] * 100) / total : 0;
		}
	}

	/**
	 * Summarise out probabilities for given number of picks
	 */
	void summariseOuts(int rem, int k) {
		// maximum number of times an out can appear
		// XXX probably wrong for k > 2
		float max = (float) Math.pow(rem - 1, k - 1);
		for (Map.Entry<String,int[]> me : outcount.entrySet()) {
			int count = me.getValue()[0];
			String card = me.getKey();
			float v = (count * 100f) / max;
			outs.put(card, v);
		}
	}
	
	public int outs(float eq) {
		int c = 0;
		for (float f : outs.values()) {
			if (f > eq) {
				c++;
			}
		}
		return c;
	}
	
	@Override
	public String toString() {
		return String.format("Eq[win %-2.1f tie %-2.1f outs=%d %s]", won, tied, outs(75f), Poker.valueString(current));
	}

}
