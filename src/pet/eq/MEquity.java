package pet.eq;

/**
 * multiple equity - represents the high and low equity of a hand
 */
public class MEquity {
	
	public final Equity hi, lo;
	// TODO add scoop
	public final boolean exact;
	public final int rem;
	/** is any low possible */
	public boolean lowPossible;
	/** total equity, including hi/lo and ties */
	public float totaleq;

	public MEquity(boolean hilo, int rem, boolean exact) {
		this.hi = new Equity();
		this.lo = hilo ? new Equity() : null;
		this.rem = rem;
		this.exact = exact;
	}
}
