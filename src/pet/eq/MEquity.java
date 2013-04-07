package pet.eq;

/**
 * multiple equity - represents the only or high/low equity of a hand
 */
public class MEquity {
	
	/** equity instances */
	public final Equity[] eqs;
	/** is this equity exact or sampled */
	public final boolean exact;
	/** number of cards remaining in deck */
	public final int remCards;
	/** is hi/hilo-hi/hilo-lo combination */
	public final boolean hilo;
	
	/** percentage of hands with low possible */
	public float lowPossible;
	/** percentage total equity, including hi/lo and ties */
	public float totaleq;
	/** percentage times the player will win the entire pot */
	public float scoop;
	
	/** number of times won all pots, no ties */
	int scoopcount;
	
	static MEquity createMEquity (Equity.Type type, int rem, boolean exact) {
		Equity[] eqs = new Equity[] {
				new Equity(type, exact)
		};
		return new MEquity(eqs, false, rem, exact);
	}
	
	static MEquity createMEquityHL (boolean hilo, int rem, boolean exact) {
		Equity[] eqs;
		if (hilo) {
			eqs = new Equity[] {
				new Equity(Equity.Type.HI_ONLY, exact), 
				new Equity(Equity.Type.HILO_HI_HALF, exact),
				new Equity(Equity.Type.HILO_AFLO8_HALF, exact)
			};
		} else {
			eqs = new Equity[] {
				new Equity(Equity.Type.HI_ONLY, exact)
			};
		}
		return new MEquity(eqs, hilo, rem, exact);
	}
	
	private MEquity(Equity[] eqs, boolean hilo, int rem, boolean exact) {
		this.eqs = eqs;
		this.hilo = hilo;
		this.remCards = rem;
		this.exact = exact;
	}
	
	/** get the equity instance for the given equity type */
	public Equity getEquity(Equity.Type type) {
		int i;
		switch (type) {
			case DSLO_ONLY:
			case AFLO_ONLY:
			case AFLO8_ONLY:
			case HI_ONLY:
			case BADUGI_ONLY:
				i = 0;
				break;
			case HILO_HI_HALF:
				i = 1;
				break;
			case HILO_AFLO8_HALF:
				i = 2;
				break;
			default:
				throw new RuntimeException("no such equity type " + type);
		}
		Equity e = eqs[i];
		if (e.type != type) {
			throw new RuntimeException("eq is type " + e.type + " not " + type);
		}
		return e;
	}
	
}
