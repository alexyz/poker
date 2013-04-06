package pet.eq;


public class FiveCardStudPoker extends Poker {
	
	
	public FiveCardStudPoker() {
		super(Value.hiValue);
	}
	
	@Override
	protected MEquity[] equity (String[] board, String[][] holeCards, String[] blockers, int draws) {
		return null;
	}
	
	@Override
	public int value (String[] board, String[] hole) {
		return 0;
	}
	
}
