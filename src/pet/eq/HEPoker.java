package pet.eq;

import java.util.Arrays;

/**
 * Hold'em and Omaha hand analysis, using a combinatorial number system.
 */
public class HEPoker extends Poker {

	/** check board is either null or 3-5 cards */
	private static void validateBoard(String[] board) {
		if (board.length > 5) {
			throw new RuntimeException("invalid board: " + Arrays.toString(board));
		}
	}
	
	/** check hole has at least 1 or 2 cards and at most 2 or 4 cards */
	private static void validateHole(String[] hole, boolean omaha) {
		final int min = omaha ? 2 : 1;
		final int max = omaha ? 5 : 2;
		if (hole.length < min || hole.length > max) {
			throw new RuntimeException("invalid hole: " + Arrays.toString(hole));
		}
	}

	//
	// instance stuff
	//
	
	/** temporary hand for the value method */
	private final String[] valueTemp = new String[5];
	private final boolean omaha;
	private final int min;
	private final boolean hilo;
	private final Value hiValue;
	private final Value loValue;

	/**
	 * create holdem equity calculator for given game type
	 */
	public HEPoker(boolean omaha, boolean hilo) {
		this(omaha, hilo, Value.hiValue, Value.afLow8Value);
	}
	
	/**
	 * create holdem equity calculator for given game type
	 */
	public HEPoker(boolean omaha, boolean hilo, Value hi, Value lo) {
		this.omaha = omaha;
		this.hiValue = hi;
		this.loValue = lo;
		this.min = omaha ? 2 : 0;
		this.hilo = hilo;
	}
	
	// sync to protect changes to temp
	@Override
	public synchronized MEquity[] equity(String[] board, String[][] holeCards, String[] blockers, int draws) {
		System.out.println("holdem/omaha equity: " + Arrays.deepToString(holeCards) + " board: " + Arrays.toString(board) + " blockers: " + Arrays.toString(blockers));
		if (draws != 0) {
			throw new RuntimeException();
		}
		
		Arrays.fill(valueTemp, null);
		validateBoard(board);
		for (String[] hole : holeCards) {
			validateHole(hole, omaha);
		}
		
		// cards not used by hands or board
		final String[] deck = Poker.remdeck(holeCards, board, blockers);
		
		if (board.length <= 1) {
			// monte carlo (random sample boards)
			return equityImpl(new HEBoardSample(deck, board, 10000), holeCards);
			
		} else {
			// all possible boards
			return equityImpl(new HEBoardEnum(deck, board), holeCards);
		}
	}

	// sync to protect changes to temp
	@Override
	public synchronized int value(String[] board, String[] hole) {
		Arrays.fill(valueTemp, null);
		validateBoard(board);
		validateHole(hole, omaha);
		
		if (board.length < 3) {
			// could use the draw poker getPair method...
			return 0;
			
		} else {
			return heValue(hiValue, board, hole);
		}
	}
	
	/**
	 * Calc exact tex/omaha hand equity for each hand for given board
	 */
	private MEquity[] equityImpl(final HEBoard heboard, final String[][] holeCards) {
		
		// XXX low possible should really be a method on Value
		boolean lowPossible = hilo;
		if (hilo) {
			if (heboard.current != null) {
				// only possible if there are no more than 2 high cards on board
				lowPossible = heboard.current.length - lowCount(heboard.current, false) <= 2;
			}
		}
		
		// equity type is ignored if hilo is true
		final MEquity[] meqs = MEquityUtil.createMEquity(holeCards.length, hilo, Equity.HI_ONLY, heboard.deck.length, heboard.exact());
		final int[] hivals = new int[holeCards.length];
		final int[] lovals = lowPossible ? new int[holeCards.length] : null;
		
		// get current high hand values (not equity)
		if (heboard.current != null) {
			for (int n = 0; n < holeCards.length; n++) {
				if (heboard.current.length >= 3) {
					hivals[n] = heValue(hiValue, heboard.current, holeCards[n]);
				}
			}
			MEquityUtil.updateCurrent(meqs, Equity.HI_ONLY, hivals);
			
			if (lowPossible) {
				MEquityUtil.updateCurrent(meqs, Equity.HILO_HI_HALF, hivals);
				// get current low values
				for (int n = 0; n < holeCards.length; n++) {
					lovals[n] = heValue(loValue, heboard.current, holeCards[n]);
				}
				MEquityUtil.updateCurrent(meqs, Equity.HILO_AFLO8_HALF, lovals);
			}
		}
		
		// get equity
		final int count = heboard.count();
		final int pick = heboard.pick();
		final String[] outs = pick <= 2 ? new String[pick] : null;
		int hiloCount = 0;
		
		for (int p = 0; p < count; p++) {
			// get board
			heboard.next();
			//System.out.println("board p: " + p + " current: " + Arrays.toString(heboard.current) + " next: " + Arrays.toString(heboard.board));
			
			// hi equity
			for (int i = 0; i < holeCards.length; i++) {
				hivals[i] = heValue(hiValue, heboard.board, holeCards[i]);
			}
			
			// low equity - only counts if at least one hand makes low
			boolean hasLow = false;
			if (lowPossible) {
				for (int i = 0; i < holeCards.length; i++) {
					int v = heValue(loValue, heboard.board, holeCards[i]);
					if (v > 0) {
						hasLow = true;
					}
					lovals[i] = v;
				}
			}
			
			// XXX this is ugly, should be in HEBoardEnum class only
			if (outs != null) {
				for (int n = 0; n < pick; n++) {
					outs[n] = heboard.board[5 - pick + n];
				}
			}
			
			if (hasLow) {
				hiloCount++;
				MEquityUtil.updateEquityHiLo(meqs, hivals, lovals, outs);
				
			} else {
				// high winner
				MEquityUtil.updateEquityHi(meqs, Equity.HI_ONLY, hivals, null);
			}
		}

		MEquityUtil.summariseEquity(meqs, count, hiloCount);
		// XXX shouldn't be here, just need to store pick and count on mequity
		MEquityUtil.summariseOuts(meqs, pick, count);
		return meqs;
	}
	

	/**
	 * Calculate value of holdem/omaha hand (using at least min cards from hand). 
	 * Board can be 3-5 cards.
	 */
	private int heValue(final Value v, final String[] board, final String[] hole) {
		int hv = 0;
		for (int n = min; n <= 2; n++) {
			final int nh = MathsUtil.binaryCoefficientFast(hole.length, n);
			final int nb = MathsUtil.binaryCoefficientFast(board.length, 5 - n);
			for (int kh = 0; kh < nh; kh++) {
				MathsUtil.kCombination(n, kh, hole, valueTemp, 0);
				for (int kb = 0; kb < nb; kb++) {
					MathsUtil.kCombination(5 - n, kb, board, valueTemp, n);
					final int val = v.value(valueTemp);
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
