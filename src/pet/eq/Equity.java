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
	
	/*
	 * equity types (note: hi/lo (8 or better) is not a type, it is actually
	 * three types, hence the MEquity class). These look similar to the
	 * constants in the Poker class, such as AF_LOW_TYPE, but they deal with
	 * hand valuation only, whereas these include the context of how that
	 * valuation is used.
	 */
	/** deuce to seven low only equity type (single draw/triple draw) */
	public static final int DSLO_ONLY = 1;
	/** ace to five low only equity type  (razz) */ 
	public static final int AFLO_ONLY = 2;
	/** high only equity type (holdem, omaha hi, 5 card draw, etc) */
	public static final int HI_ONLY = 3;
	/** high half of hi/lo equity type (omaha 8, stud 8, etc) */
	public static final int HILO_HI_HALF = 4;
	/** ace to five low 8 or better half of hi/lo equity type (omaha 8, stud 8, etc) */ 
	public static final int HILO_AFLO8_HALF = 5;
	/** ace to five low 8 or better only equity type (not used alone by any game, as it's qualified) */
	public static final int AFLO8_ONLY = 6;
	
	/** get name of equity type */
	public static String getEqTypeName(int eqtype) {
		switch (eqtype) {
			case DSLO_ONLY: return "2-7 Low Only";
			case AFLO_ONLY: return "A-5 Low Only";
			case AFLO8_ONLY: return "A-5 Low (8) Only";
			case HI_ONLY: return "High Only";
			case HILO_HI_HALF: return "High Half";
			case HILO_AFLO8_HALF: return "A-5 Low (8) Half";
			default: throw new RuntimeException("no such equity type: " + eqtype);
		}
	}
	
	/**
	 * get the array of rank names for the equity type. can't use current value
	 * to get type because it might not be set
	 */
	public static String[] getRankNames(int eqtype) {
		switch (eqtype) {
			case DSLO_ONLY: 
				return Poker.dsLowRankNames;
			case AFLO_ONLY:
			case HILO_AFLO8_HALF:
			case AFLO8_ONLY: 
				return Poker.afLowRankNames;
			case HI_ONLY:
			case HILO_HI_HALF:
				return Poker.ranknames;
			default: 
				throw new RuntimeException("no such equity type: " + eqtype);
		}
	}
	
	/** equity type description */
	public final int eqtype;
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
	/** map of cards to percentage times that card will make best hand */
	public final List<Out> outs = new ArrayList<Out>();
	
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
	final int[] outcount = new int[52];
	
	public Equity(int eqtype) {
		this.eqtype = eqtype;
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
		// maximum number of times an out can appear (average if sampled)
		// prob of appearing once is picks/remCards, just multiply by samples
		// (n,k,s) = (k*s)/n
		// (52,1,52) = 1,  (52,2,1326) = 51,  (52,3,100000) = 5769 
		float max = (picks * samples) / remCards;
		System.out.println(String.format("sum outs(%f,%f,%f) max=%f", remCards, picks, samples, max));
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
		System.out.println("outs are " + outs);
	}
	
}
