package pet.eq;

import java.util.List;

/**
 * poker hand value and draw prediction function
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
		@Override
		public String[] draw (String[] cards, int drawn, String[] blockers, List<Draw> drawList) {
			return DrawPrediction.getDrawingHand(drawList, cards, drawn, true, blockers);
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
		// draw - just the lowest unique cards?
	};
	
	/**
	 * Calculates ace to five low 8 or better value of hand
	 */
	public static final Value afLow8Value = new Value(Equity.Type.AFLO8_ONLY, 5) {
		@Override
		public final int value(String[] hand) {
			return Poker.afLow8Value(hand);
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
		@Override
		public String[] draw (String[] cards, int drawn, String[] blockers, List<Draw> drawList) {
			return DrawPrediction.getDrawingHand(drawList, cards, drawn, false, blockers);
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
	 * badugi value
	 */
	public static final Value badugiValue = new Value(Equity.Type.BADUGI_ONLY, 4) {
		@Override
		public int value(String[] hand) {
			return Badugi.badugiValue(hand);
		}
		@Override
		public String[] draw (String[] cards, int drawn, String[] blockers, List<Draw> drawList) {
			return Badugi.draw(cards, drawn);
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
	
	/**
	 * get estimated drawing hand. this method is on Value and not Poker because
	 * it largely depends on the valuation method rather than the game rules
	 */
	public String[] draw(String[] cards, int drawn, String[] blockers, List<Draw> drawList) {
		throw new RuntimeException("yawn");
	}
}
