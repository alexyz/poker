package pet.eq;


public class BadugiPoker extends Poker {
	
	public static final String[] shortRanks = { "B:4", "B:5", "B:6", "B:7", "B", "3", "2/1" };
	
	@Override
	protected MEquity[] equity (String[] board, String[][] holeCards, String[] blockers, int draws) {
		return null;
	}
	
	@Override
	public int value (String[] board, String[] hole) {
		return 0;
	}
	
}
