
package pet.eq.impl;

import pet.eq.*;

/**
 * five stud is virtually the same as draw
 */
public class FiveStudPoker extends DrawPoker {
	
	public FiveStudPoker() {
		super(Value.hiValue);
	}
	
	@Override
	protected MEquity[] equity (String[] board, String[][] holeCards, String[] blockers, int draws) {
		// pretend we are doing one draw
		return super.equity(board, holeCards, blockers, 1);
	}
	
}
