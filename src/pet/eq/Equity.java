package pet.eq;

import java.util.*;

/**
 * Represents the equity of a hand
 */
public class Equity {
	
	/** equity type description */
	public final String type;
	/** current value */
	public int current;
	/** Currently winning and not tying */
	public boolean curwin;
	/** currently tying */
	public boolean curtie;
	/** percentage of hands won but not tied */
	public float won;
	/** percentage of hands tied but not won */
	public float tied;
	/** total equity percentage */
	public float total;
	/** percentage of hands won or tied by rank (value >> 20) */
	public final float[] wonrank = new float[Poker.RANKS];
	/** percentage that each card will make best hand */
	public final Map<String,Float> outs = new TreeMap<String,Float>();
	
	// transient stuff
	
	/** number of samples won */
	int woncount;
	/** number of samples tied */
	int tiedcount;
	/** number of people tied with including self */
	int tiedwithcount;
	// XXX hi only
	/** winning ranks */
	final int[] wonrankcount = new int[Poker.RANKS];
	/** count that each card (as part of group of k cards) will make the best hand */
	final Map<String,int[]> outcount = new TreeMap<String,int[]>();
	
	public Equity(String type) {
		this.type = type;
	}

	/**
	 * update percentage won, tied and by rank
	 */
	void summariseEquity(int hands) {
		int wontiedcount = woncount + tiedcount;
		won = (woncount * 100f) / hands;
		tied = (tiedcount * 100f) / hands;
		for (int n = 0; n < wonrankcount.length; n++) {
			wonrank[n] = wontiedcount != 0 ? (wonrankcount[n] * 100f) / wontiedcount : 0;
		}
		
		total = (woncount * 100f) / hands;
		System.out.println("    hands=" + hands + " woncount=" + woncount + " totalpc=" + total);
		if (tiedcount > 0) {
			total += (tied * ((tiedcount * 1f) / tiedwithcount));
			System.out.println("    hands=" + hands + " woncount=" + woncount + " tiedcount=" + tiedcount + " tiedwithcount=" + tiedwithcount + " totalpc=" + total);
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
	
}
