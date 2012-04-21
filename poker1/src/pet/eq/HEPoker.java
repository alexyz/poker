package pet.eq;

import java.util.Arrays;

/**
 * Hold'em and Omaha hand analysis, using a combinatorial number system.
 */
public class HEPoker extends Poker {

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
	// instance stuff
	//
	
	private String[] temp = new String[5];
	private final boolean omaha;
	private final int min;
	private final boolean hilo;

	/**
	 * create holdem equity calculator for given game type
	 */
	public HEPoker(boolean omaha, boolean hilo) {
		this.omaha = omaha;
		this.min = omaha ? 2 : 0;
		this.hilo = hilo;
	}
	
	@Override
	public MEquity[] equity(String[] board, String[][] holes, String[] blockers) {
		validateBoard(board);
		for (String[] hole : holes) {
			validateHole(hole, omaha);
		}
		
		// cards not used by hands or board
		final String[] deck = ArrayUtil.remove(Poker.FULL_DECK, board, holes, blockers);
		
		if (board == null) {
			return sampleEquity(deck, holes);
		} else {
			return exactEquity(deck, board, holes);
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
			return value(Poker.hi, board, hole);
		}
	}
	
	/**
	 * Calc exact tex/omaha hand equity for each hand for given board
	 */
	private MEquity[] exactEquity(final String[] deck, final String[] board, final String[][] holes) {
		final MEquity[] meqs = MEquityUtil.makeMEquity(holes.length, hilo, deck.length, true);
		final int[] vals = new int[holes.length];
		
		// get current high hand values (not equity)
		for (int n = 0; n < holes.length; n++) {
			vals[n] = value(Poker.hi, board, holes[n]);
		}
		MEquityUtil.updateCurrent(meqs, true, vals);
		
		// get current low values
		if (hilo) {
			for (int n = 0; n < holes.length; n++) {
				vals[n] = value(Poker.lo, board, holes[n]);
			}
			MEquityUtil.updateCurrent(meqs, false, vals);
		}
		
		// get equity
		final String[] tempBoard = Arrays.copyOf(board, 5);
		final int k = 5 - board.length;
		final int combs = MathsUtil.bincoff(deck.length, k);
		for (int p = 0; p < combs; p++) {
			// hi equity
			MathsUtil.kcomb(k, p, deck, tempBoard, board.length);
			for (int i = 0; i < holes.length; i++) {
				vals[i] = value(Poker.hi, tempBoard, holes[i]);
			}
			MEquityUtil.updateEquity(meqs, true, vals, tempBoard, board.length);
			// low equity
			if (hilo) {
				for (int i = 0; i < holes.length; i++) {
					vals[i] = value(Poker.lo, tempBoard, holes[i]);
				}
				MEquityUtil.updateEquity(meqs, false, vals, tempBoard, board.length);
			}
		}

		MEquityUtil.summariseEquity(meqs, combs);
		MEquityUtil.summariseOuts(meqs, k);
		return meqs;
	}

	/**
	 * Calc sampled tex/omaha hand equity for each hand by generating random boards
	 */
	private MEquity[] sampleEquity(final String[] deck, final String[][] holes) {
		final String[] board = new String[5];
		final MEquity[] meqs = MEquityUtil.makeMEquity(holes.length, hilo, deck.length, false);
		// hand values for a particular board
		final int[] vals = new int[holes.length];
		final long[] picked = new long[1];
		final int sz = 1000;

		for (int p = 0; p < sz; p++) {
			picked[0] = 0;
			for (int n = 0; n < 5; n++) {
				// TODO should really pick straight into board
				// should also use thread local random
				board[n] = RandomUtil.pick(deck, picked);
			}
			for (int i = 0; i < holes.length; i++) {
				vals[i] = value(Poker.hi, board, holes[i]);
			}
			MEquityUtil.updateEquity(meqs, true, vals, null, 0);
			if (hilo) {
				for (int i = 0; i < holes.length; i++) {
					vals[i] = value(Poker.lo, board, holes[i]);
				}
				MEquityUtil.updateEquity(meqs, false, vals, null, 0);
			}
		}

		MEquityUtil.summariseEquity(meqs, sz);
		return meqs;
	}

	/**
	 * Calculate value of holdem/omaha hand (using at least min cards from hand). 
	 * Temp must be 5 element array
	 */
	private int value(Value v, String[] board, String[] hole) {
		int hv = 0;
		for (int n = min; n <= 2; n++) {
			final int nh = MathsUtil.bincoff(hole.length, n);
			final int nb = MathsUtil.bincoff(board.length, 5 - n);
			for (int kh = 0; kh < nh; kh++) {
				MathsUtil.kcomb(n, kh, hole, temp, 0);
				for (int kb = 0; kb < nb; kb++) {
					MathsUtil.kcomb(5 - n, kb, board, temp, n);
					final int val = v.value(temp);
					//System.out.println(Arrays.asList(h5) + " - " + Poker.desc(v));
					if (val > hv) {
						hv = val;
					}
				}
			}
		}
		return hv;
	}
	
}
