package pet.eq;

/**
 * 5 card hand value function
 */
public abstract class Value {
	
	/**
	 * calculates high value of hand
	 */
	public static final Value hiValue = new Value(Equity.Type.HI_ONLY, 5) {
		@Override
		public final int value(String[] hand) {
			return Poker.value(hand);
		}
	};
	
	/**
	 * calculates unconditional ace to five low value of hand
	 */
	public static final Value afLowValue = new Value(Equity.Type.AFLO_ONLY, 5) {
		@Override
		public int value(String[] hand) {
			return Poker.afLowValue(hand);
		}
	};
	
	/**
	 * Calculates ace to five low 8 or better value of hand
	 */
	public static final Value afLow8Value = new Value(Equity.Type.AFLO8_ONLY, 5) {
		@Override
		public final int value(String[] hand) {
			return Poker.aflow8Value(hand);
		}
	};
	
	/**
	 * deuce to seven low value function
	 */
	public static final Value dsLowValue = new Value(Equity.Type.DSLO_ONLY, 5) {
		@Override
		public int value(String[] hand) {
			return Poker.dsValue(hand);
		}
	};
	
	/**
	 * straight hi value only (hi equity type)
	 */
	public static final Value strHiValue = new Value(Equity.Type.HI_ONLY, 5) {
		@Override
		public int value(String[] hand) {
			return Poker.strValue(hand);
		}
	};
	
	/**
	 * straight hi value only (hi equity type)
	 */
	public static final Value badugiValue = new Value(Equity.Type.BADUGI_ONLY, 4) {
		@Override
		public int value(String[] hand) {
			return Badugi.badugiValue(hand);
		}
	};
	
	/**
	 * default (non-hi/lo) equity type for this valuation function - see Equity
	 * class constants
	 */
	public final Equity.Type eqtype;
	/** number of cards required by value function */
	public final int cards;
	
	private Value(Equity.Type eqtype, int cards) {
		this.eqtype = eqtype;
		this.cards = cards;
	}
	
	/**
	 * get hand value from subclass
	 */
	public abstract int value(String[] hand);
}
