package pet.eq;

/**
 * represents the high and low equity of a hand
 */
public class MEquity {
	
	public final Equity hi, lo;
	// TODO add scoop, total eq
	public final boolean exact;
	public final int rem;

	public MEquity(boolean hilo, int rem, boolean exact) {
		this.hi = new Equity();
		this.lo = hilo ? new Equity() : null;
		this.rem = rem;
		this.exact = exact;
	}
}
