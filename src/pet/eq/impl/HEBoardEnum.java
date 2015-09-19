package pet.eq.impl;

import pet.eq.MathsUtil;

/**
 * generate all possible remaining boards
 */
class HEBoardEnum extends HEBoard {
	/** number of boards */
	private final int count;
	/** number of cards to pick */
	private final int k;
	/** board number */
	private int p = 0;
	
	public HEBoardEnum(String[] deck, String[] current) {
		super(deck, current);
		for (int n = 0; n < current.length; n++) {
			board[n] = current[n];
		}
		k = 5 - current.length;
		count = MathsUtil.binomialCoefficientFast(deck.length, k);
	}
	
	@Override
	int count() {
		return count;
	}
	
	@Override
	int pick() {
		return k;
	}
	
	@Override
	void next() {
		// get board combination
		MathsUtil.kCombination(k, p++, deck, board, current.length);
	}
	
	@Override
	boolean exact() {
		return true;
	}
}