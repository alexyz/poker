package pet.eq.impl;

import java.util.Random;

import pet.eq.ArrayUtil;

/**
 * generate sample remaining boards
 */
class HEBoardSample extends HEBoard {
	private final long[] picked = new long[1];
	private final int count;
	private final Random r = new Random();
	
	public HEBoardSample(String[] deck, String[] current, int count) {
		super(deck, current);
		this.count = count;
		for (int n = 0; n < current.length; n++) {
			board[n] = current[n];
		}
	}
	
	@Override
	int count() {
		return count;
	}
	
	@Override
	int pick() {
		return 5;
	}

	@Override
	void next() {
		picked[0] = 0;
		for (int n = current.length; n < 5; n++) {
			board[n] = ArrayUtil.pick(r, deck, picked);
		}
	}
	
	@Override
	boolean exact() {
		return false;
	}
	
}