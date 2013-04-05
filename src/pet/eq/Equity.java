package pet.eq;

import java.util.*;

/**
 * Represents the equity of a hand according to a specific valuation type
 */
public class Equity {
	
	public static class Out implements Comparable<Out> {
		public final float pc;
		public final String card;
		public Out(String card, float pc) {
			this.card = card;
			this.pc = pc;
		}
		@Override
		public int compareTo(Out o) {
			float c = pc - o.pc;
			if (c != 0) {
				return (int) Math.signum(c);
			}
			return Cmp.cardCmp.compare(card, o.card);
		}
		@Override
		public String toString() {
			return String.format("%s[%.1f]", card, pc);
		}
	}
	
	/**
	 * equity types (note: hi/lo (8 or better) is not a type, it is actually
	 * three types, hence the MEquity class). These look similar to the
	 * constants in the Poker class, such as AF_LOW_TYPE, but they deal with
	 * hand valuation only, whereas these include the context of how that
	 * valuation is used.
	 */
	public enum Type {
		/** deuce to seven low only equity type (single draw/triple draw) */
		DSLO_ONLY,
		/** ace to five low only equity type  (razz) */ 
		AFLO_ONLY,
		/** high only equity type (holdem, omaha hi, 5 card draw, etc) */
		HI_ONLY,
		/** high half of hi/lo equity type (omaha 8, stud 8, etc) */
		HILO_HI_HALF,
		/** ace to five low 8 or better half of hi/lo equity type (omaha 8, stud 8, etc) */ 
		HILO_AFLO8_HALF,
		/** ace to five low 8 or better only equity type (not used alone by any game, as it's qualified) */
		AFLO8_ONLY
	}
	/** equity type description */
	public final Type eqtype;
	/** current value */
	public int current;
	/** Currently winning and not tying */
	public boolean curwin;
	/** currently tying */
	public boolean curtie;
	/**
	 * percentage of hands won but not tied. note: for hi/lo, it is possible to
	 * just add hi only won and hi half won for total high won, as they are
	 * exclusive. same for tied.
	 */
	public float won;
	/** percentage of hands tied but not won */
	public float tied;
	/** total equity percentage */
	public float total;
	/** percentage of hands won or tied by rank (value >> 20) */
	public final float[] wonrank = new float[Poker.RANKS];
	/** list of cards and percentage times that card is included in a pick that will make best hand */
	public final List<Out> outs;
	
	// transient stuff
	
	/** number of samples won */
	int woncount;
	/** number of samples tied */
	int tiedcount;
	/** number of people tied with including self */
	int tiedwithcount;
	/** winning ranks */
	final int[] wonrankcount = new int[Poker.RANKS];
	/** count that each card (as part of group of cards) will make the best hand */
	final int[] outcount;
	
	public Equity(Equity.Type eqtype, boolean hasouts) {
		this.eqtype = eqtype;
		this.outcount = hasouts ? new int[52] : null;
		this.outs = hasouts ? new ArrayList<Out>() : null;
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
		//System.out.println("    hands=" + hands + " woncount=" + woncount + " totalpc=" + total);
		if (tiedcount > 0) {
			total += (tied * ((tiedcount * 1f) / tiedwithcount));
			//System.out.println("    hands=" + hands + " woncount=" + woncount + " tiedcount=" + tiedcount + " tiedwithcount=" + tiedwithcount + " totalpc=" + total);
		}
	}
	
	/**
	 * Summarise out probabilities for given number of picks from remaining cards
	 */
	void summariseOuts(float remCards, float picks, float samples) {
		if (outcount != null) {
			// maximum number of times an out can appear (average if sampled)
			// prob of appearing once is picks/remCards, just multiply by samples
			// (n,k,s) = (k*s)/n
			// (52,1,52) = 1,  (52,2,1326) = 51,  (52,3,100000) = 5769 
			float max = (picks * samples) / remCards;
			//System.out.println(String.format("sum outs(%f,%f,%f) max=%f", remCards, picks, samples, max));
			for (int n = 0; n < outcount.length; n++) {
				int count = outcount[n];
				if (count > 0) {
					String card = Poker.indexToCard(n);
					float pc = (count * 100f) / max;
					outs.add(new Out(card, pc));
				}
			}
			Collections.sort(outs);
			Collections.reverse(outs);
			//System.out.println("outs are " + outs);
		}
	}
	
}
