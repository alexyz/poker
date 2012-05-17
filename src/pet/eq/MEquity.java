package pet.eq;

/**
 * multiple equity - represents the high and low equity of a hand
 */
public class MEquity {
	
	public static final int HIONLY = 0, HIHALF = 1, LOHALF = 2;
	public final Equity[] eq;
	/** is this equity exact or sampled */
	public final boolean exact;
	/** number of cards remaining in deck */
	public final int remCards;
	/** percentage of hands with low possible */
	public float lowPossible;
	/** percentage total equity, including hi/lo and ties */
	public float totaleq;
	/** percentage times the player will win the entire pot */
	public float scoop;
	
	/** number of times won all pots, no ties */
	int scoopcount;

	public MEquity(boolean hilo, int rem, boolean exact) {
		if (hilo) {
			eq = new Equity[] { new Equity("Hi Only"), new Equity("Hi Half"), new Equity("Lo Half") };
		} else {
			eq = new Equity[] { new Equity("Hi Only") };
		}
		this.remCards = rem;
		this.exact = exact;
	}
	
	public boolean hilo() {
		return eq.length > 1;
	}
	
	public Equity hionly() {
		return eq[HIONLY];
	}
	
	public Equity hihalf() {
		return eq.length > 1 ? eq[HIHALF] : null;
	}
	
	public Equity lohalf() {
		return eq.length > 1 ? eq[LOHALF] : null;
	}
}
