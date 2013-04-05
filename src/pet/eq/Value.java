package pet.eq;

/**
 * 5 card hand value function
 */
public abstract class Value {
	
	/**
	 * calculates high value of hand
	 */
	public static final Value hiValue = new Value(Equity.Type.HI_ONLY) {
		@Override
		public final int value(String[] hand) {
			return Poker.value(hand);
		}
	};
	
	/**
	 * calculates unconditional ace to five low value of hand
	 */
	public static final Value afLowValue = new Value(Equity.Type.AFLO_ONLY) {
		@Override
		public int value(String[] hand) {
			return Poker.afLowValue(hand);
		}
	};
	
	/**
	 * Calculates ace to five low 8 or better value of hand
	 */
	public static final Value afLow8Value = new Value(Equity.Type.AFLO8_ONLY) {
		@Override
		public final int value(String[] hand) {
			return Poker.aflow8Value(hand);
		}
	};
	
	/**
	 * deuce to seven low value function
	 */
	public static final Value dsLowValue = new Value(Equity.Type.DSLO_ONLY) {
		@Override
		public int value(String[] hand) {
			return Poker.dsValue(hand);
		}
	};
	
	/**
	 * straight hi value only (hi equity type)
	 */
	public static final Value strHiValue = new Value(Equity.Type.HI_ONLY) {
		@Override
		public int value(String[] hand) {
			return Poker.strValue(hand);
		}
	};
	
	private final Equity.Type eqtype;
	
	private Value(Equity.Type eqtype) {
		this.eqtype = eqtype;
	}
	
	/**
	 * default (non-hi/lo) equity type for this valuation function - see Equity
	 * class constants
	 */
	public final Equity.Type eqtype() {
		return eqtype;
	}
	/**
	 * get hand value
	 */
	public abstract int value(String[] hand);
}
