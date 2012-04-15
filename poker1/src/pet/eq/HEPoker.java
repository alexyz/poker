package pet.eq;

import java.util.Arrays;

/**
 * Hold'em and Omaha hand analysis, using a combinatorial number system.
 */
public class HEPoker extends Poker {

	/**
	 * Calc exact tex/omaha hand equity for each hand for given board
	 */
	private static HandEq[] exactEquity(final String[] board, final String[][] holes, final int min, final String[] blockers) {
		// cards not used by hands or board
		final String[] deck = ArrayUtil.remove(Poker.FULL_DECK, board, holes, blockers);
		final HandEq[] eqs = HandEq.makeHandEqs(holes.length, deck.length, true);
		final String[] tempHand = new String[5];
		
		// get current hand values (not equity)
		final int[] vals = new int[holes.length];
		for (int n = 0; n < holes.length; n++) {
			vals[n] = value(board, holes[n], min, tempHand);
		}
		HandEq.updateCurrent(eqs, vals);

		// get equity
		final String[] tempBoard = Arrays.copyOf(board, 5);
		final int k = 5 - board.length;
		final int combs = MathsUtil.bincoff(deck.length, k);
		for (int p = 0; p < combs; p++) {
			MathsUtil.kcomb(k, p, deck, tempBoard, board.length);
			for (int i = 0; i < holes.length; i++) {
				vals[i] = value(tempBoard, holes[i], min, tempHand);
			}
			HandEq.updateEquities(eqs, vals, tempBoard, board.length);
		}

		HandEq.summariseEquities(eqs, combs);
		HandEq.summariseOuts(eqs, k);
		return eqs;
	}

	/**
	 * Calc sampled tex/omaha hand equity for each hand by generating random boards
	 */
	private static HandEq[] sampleEquity(final String[][] holes, int min, final String[] blockers) {
		final String[] deck = ArrayUtil.remove(Poker.FULL_DECK, null, holes, blockers);
		final String[] board = new String[5];
		final HandEq[] eqs = HandEq.makeHandEqs(holes.length, deck.length, false);
		final String[] temp = new String[5];

		// hand values for a particular board
		final int[] vals = new int[holes.length];
		final long[] picked = new long[1];
		final int sz = 1000;

		for (int p = 0; p < sz; p++) {
			picked[0] = 0;
			for (int n = 0; n < 5; n++) {
				board[n] = RandomUtil.pick(deck, picked);
			}
			for (int i = 0; i < holes.length; i++) {
				vals[i] = value(board, holes[i], min, temp);
			}
			HandEq.updateEquities(eqs, vals);
		}

		HandEq.summariseEquities(eqs, sz);
		return eqs;
	}

	/**
	 * Calculate value of holdem/omaha hand (using at least min cards from hand). 
	 * Temp must be 5 element array
	 */
	private static int value(String[] board, String[] hole, int min, String[] temp) {
		int hv = 0;
		for (int n = min; n <= 2; n++) {
			final int nh = MathsUtil.bincoff(hole.length, n);
			final int nb = MathsUtil.bincoff(board.length, 5 - n);
			for (int kh = 0; kh < nh; kh++) {
				MathsUtil.kcomb(n, kh, hole, temp, 0);
				for (int kb = 0; kb < nb; kb++) {
					MathsUtil.kcomb(5 - n, kb, board, temp, n);
					final int v = value(temp);
					//System.out.println(Arrays.asList(h5) + " - " + Poker.desc(v));
					if (v > hv) {
						hv = v;
					}
				}
			}
		}
		return hv;
	}
	
	/** check board is either null or 3-5 cards */
	private static void validateBoard(String[] board) {
		if (board != null && (board.length < 3 || board.length > 5)) {
			throw new RuntimeException("invalid board: " + Arrays.toString(board));
		}
	}
	
	/** check hole has at least 1 or 2 cards and at most 2 or 4 cards */
	private static void validateHole(String[] hole, boolean omaha) {
		final int min = omaha ? 2 : 1;
		final int max = omaha ? 4 : 2;
		if (hole.length < min || hole.length > max) {
			throw new RuntimeException("invalid hole: " + Arrays.toString(hole));
		}
	}

	
	//
	// instance methods
	//
	
	private final boolean omaha;

	/**
	 * create equity calculator for given game type
	 */
	public HEPoker(boolean omaha) {
		this.omaha = omaha;
	}
	
	@Override
	public HandEq[] equity(String[] board, String[][] holes, String[] blockers) {
		System.out.println("board=" + Arrays.toString(board));
		validateBoard(board);
		for (String[] hole : holes) {
			validateHole(hole, omaha);
		}
		
		final int min = omaha ? 2 : 0;
		if (board == null) {
			return sampleEquity(holes, min, blockers);
			
		} else {
			return exactEquity(board, holes, min, blockers);
		}
	}

	@Override
	public int value(String[] board, String[] hole) {
		validateBoard(board);
		validateHole(hole, omaha);
		
		if (board == null) {
			// could use the draw poker getPair method...
			return 0;
			
		} else {
			final int min = omaha ? 2 : 0;
			return value(board, hole, min, new String[5]);
		}
	}
	
}
