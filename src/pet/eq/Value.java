package pet.eq;

/**
 * 5 card hand value function
 */
public abstract class Value {
	
	/**
	 * calculates high value of hand
	 */
	public static final Value hiValue = new Value(Equity.HI_ONLY) {
		@Override
		public final int value(String[] hand) {
			return Poker.value(hand);
		}
	};
	
	/**
	 * calculates unconditional ace to five low value of hand
	 */
	public static final Value afLowValue = new Value(Equity.AFLO_ONLY) {
		@Override
		public int value(String[] hand) {
			return Poker.afLowValue(hand);
		}
	};
	
	/**
	 * Calculates ace to five low 8 or better value of hand
	 */
	public static final Value afLow8Value = new Value(Equity.AFLO8_ONLY) {
		@Override
		public final int value(String[] hand) {
			return Poker.aflow8Value(hand);
		}
	};
	
	/**
	 * deuce to seven low value function
	 */
	public static final Value dsLowValue = new Value(Equity.DSLO_ONLY) {
		@Override
		public int value(String[] hand) {
			return Poker.dsValue(hand);
		}
	};
	
	/**
	 * straight hi value only (hi equity type)
	 */
	public static final Value strHiValue = new Value(Equity.HI_ONLY) {
		@Override
		public int value(String[] hand) {
			return Poker.strValue(hand);
		}
	};
	
	private final int eqtype;
	
	private Value(int eqtype) {
		this.eqtype = eqtype;
	}
	
	/**
	 * default (non-hi/lo) equity type for this valuation function - see Equity
	 * class constants
	 */
	public final int eqtype() {
		return eqtype;
	}
	/**
	 * get hand value
	 */
	public abstract int value(String[] hand);
}
