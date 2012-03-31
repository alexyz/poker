package pet.eq;

import java.util.*;

/**
 * Represents the equity of a hand
 */
public class HandEq {
	
	/**
	 * Make array of hand equities for given number of remaining cards and calculation method
	 */
	static HandEq[] makeHandEqs(int sz, int rem, boolean exact) {
		HandEq[] ret = new HandEq[sz];
		for (int n = 0; n < ret.length; n++) {
			HandEq e = new HandEq();
			e.exact = exact;
			e.rem = rem;
			ret[n] = e;
		}
		return ret;
	}
	
	/**
	 * Set the current value of the hands, not the equity
	 */
	public static void updateCurrent(HandEq[] eqs, int[] vals) {
		int max = 0, times = 0;
		for (int i = 0; i < vals.length; i++) {
			int v = vals[i];
			if (v > max) {
				max = v;
				times = 1;
			} else if (v == max) {
				times++;
			}
		} 
		for (int i = 0; i < vals.length; i++) {
			HandEq e = eqs[i];
			e.current = vals[i];
			if (e.current == max) {
				if (times == 1) {
					e.curwin = true;
				} else {
					e.curtie = true;
				}
			}
		}
	}
	
	/**
	 * Update equities win, tie and win rank
	 */
	static void updateEquities(HandEq[] eqs, int[] vals) {
		updateEquities(eqs, vals, null, 0);
	}

	/**
	 * Update equities win, tie and win rank with given hand values for the given cards
	 */
	static void updateEquities(HandEq[] eqs, int[] vals, String[] cards, int off) {
		int max = 0, maxcount = 0;
		for (int i = 0; i < vals.length; i++) {
			int v = vals[i];
			if (v > max) {
				max = v;
				maxcount = 1;
			} else if (v == max) {
				maxcount++;
			}
		}
		for (int i = 0; i < vals.length; i++) {
			if (vals[i] == max) {
				HandEq e = eqs[i];
				if (maxcount == 1) {
					e.woncount++;
				} else {
					e.tiedcount++;
				}
				e.wonrankcount[max >> 20]++;
				
				// count the cards as outs if this turns losing hand into win/tie or tying hand into win
				if (cards != null && (!e.curwin || (e.curtie && maxcount == 1))) {
					for (int c = off; c < cards.length; c++) {
						String card = cards[c];
						int[] count = e.outcount.get(card);
						if (count == null) {
							e.outcount.put(card, count = new int[1]);
						}
						count[0]++;
					}
				}
			}
		}
	}

	/**
	 * Summarise equities win, tie and winrank as a percentage of nb
	 */
	static void summariseEquities(HandEq[] eqs, int nb) {
		for (HandEq e : eqs) {
			e.summariseEquity(nb);
		}
	}
	
	/**
	 * Summarise out probabilities for given number of picks
	 */
	static void summariseOuts(HandEq[] eqs, int k) {
		for (HandEq e : eqs) {
			// maximum number of times an out can appear
			// XXX probably wrong for k > 2
			float max = (float) Math.pow(e.rem - 1, k - 1);
			for (Map.Entry<String,int[]> me : e.outcount.entrySet()) {
				int count = me.getValue()[0];
				String card = me.getKey();
				float v = (count * 100f) / max;
				e.outs.put(card, v);
			}
		}
	}
	
	/** number of remaining cards */
	public int rem;
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
	/** exact result */
	public boolean exact;
	/** percentage that each card will make best hand */
	public final Map<String,Float> outs = new TreeMap<String,Float>();
	
	private int woncount;
	private int tiedcount;
	private final int[] wonrankcount = new int[9];
	/** count that each card (as part of group of k cards) will make the best hand */
	private final Map<String,int[]> outcount = new TreeMap<String,int[]>();

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
