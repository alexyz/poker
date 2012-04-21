package pet.eq;

/**
 * represents the high and low value of a hand
 */
public class MEquity {
	
	public final Equity hi, lo;
	// TODO add scoop, total eq
	public boolean exact;
	public int rem;

	public MEquity(boolean hilo, int rem, boolean exact) {
		this.hi = new Equity();
		this.lo = hilo ? new Equity() : null;
		this.rem = rem;
		this.exact = exact;
	}
}
