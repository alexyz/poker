package pet.eq;

import java.util.Random;

/**
 * methods for getting complete hold'em/omaha boards, either
 * randomly or with combinatorial enumeration
 */
abstract class HEBoard {
	/** starting board, never changes */
	final String[] current;
	/** remaining cards in deck, never changes */
	final String[] deck;
	/** next board after call to next() */
	final String[] board = new String[5];
	public HEBoard(String[] deck, String[] current) {
		this.deck = deck;
		this.current = current;
	}
	/** how many boards are there */
	abstract int count();
	/** create the next board */
	abstract void next();
}

class HEBoardSample extends HEBoard {
	private final long[] picked = new long[1];
	private final int count;
	private final Random r = new Random();
	
	public HEBoardSample(String[] deck, int count) {
		super(deck, null);
		this.count = count;
	}
	
	@Override
	int count() {
		return count;
	}

	@Override
	void next() {
		picked[0] = 0;
		for (int n = 0; n < 5; n++) {
			board[n] = ArrayUtil.pick(r, deck, picked);
		}
	}
	
}

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
		count = MathsUtil.bincoff(deck.length, k);
	}
	
	@Override
	int count() {
		return count;
	}
	
	@Override
	void next() {
		// get board combination
		MathsUtil.kcomb(k, p++, deck, board, current.length);
	}
}
	
