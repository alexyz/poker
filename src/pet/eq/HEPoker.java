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
	
	/** temporary hand for the value method */
	private final String[] valueTemp = new String[5];
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
	
	// sync to protect changes to temp
	@Override
	public synchronized MEquity[] equity(String[] board, String[][] holeCards, String[] blockers) {
		Arrays.fill(valueTemp, null);
		validateBoard(board);
		for (String[] hole : holeCards) {
			validateHole(hole, omaha);
		}
		
		// cards not used by hands or board
		final String[] deck = Poker.remdeck(holeCards, board, blockers);
		
		if (board == null) {
			// monte carlo (random sample boards)
			return equityImpl(new HEBoardSample(deck, 10000), holeCards);
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
		
		if (board == null) {
			// could use the draw poker getPair method...
			return 0;
			
		} else {
			return heValue(Value.hiValue, board, hole);
		}
	}
	
	/**
	 * Calc exact tex/omaha hand equity for each hand for given board
	 */
	private MEquity[] equityImpl(final HEBoard heboard, final String[][] holeCards) {
		boolean lowPossible = hilo;
		if (hilo) {
			if (heboard.current != null) {
				// only possible if there are no more than 2 high cards on board
				lowPossible = heboard.current.length - lowCount(heboard.current, false) <= 2;
			}
		}
		
		// equity type is ignored if hilo is true
		final MEquity[] meqs = MEquityUtil.makeMEquity(holeCards.length, hilo, Equity.HI_ONLY, heboard.deck.length, true);
		final int[] hivals = new int[holeCards.length];
		final int[] lovals = lowPossible ? new int[holeCards.length] : null;
		
		// get current high hand values (not equity)
		if (heboard.current != null) {
			for (int n = 0; n < holeCards.length; n++) {
				if (heboard.current.length >= 3) {
					hivals[n] = heValue(Value.hiValue, heboard.current, holeCards[n]);
				}
			}
			MEquityUtil.updateCurrent(meqs, Equity.HI_ONLY, hivals);
			
			// get current low values
			if (lowPossible) {
				for (int n = 0; n < holeCards.length; n++) {
					lovals[n] = heValue(Value.afLowValue, heboard.current, holeCards[n]);
				}
				MEquityUtil.updateCurrent(meqs, Equity.HILO_AFLO8_HALF, lovals);
			}
		}
		
		// get equity
		final int count = heboard.count();
		int hiloCount = 0;
		
		for (int p = 0; p < count; p++) {
			// get board
			heboard.next();
			//System.out.println("board p: " + p + " current: " + Arrays.toString(heboard.current) + " next: " + Arrays.toString(heboard.board));
			
			// hi equity
			for (int i = 0; i < holeCards.length; i++) {
				hivals[i] = heValue(Value.hiValue, heboard.board, holeCards[i]);
			}
			
			// low equity - only counts if at least one hand makes low
			boolean hasLow = false;
			if (lowPossible) {
				for (int i = 0; i < holeCards.length; i++) {
					int v = heValue(Value.afLowValue, heboard.board, holeCards[i]);
					if (v > 0) {
						hasLow = true;
					}
					lovals[i] = v;
				}
			}
			
			if (hasLow) {
				hiloCount++;
				// high winner
				int hw = MEquityUtil.updateEquity(meqs, Equity.HILO_HI_HALF, hivals, null, 0);
				// low winner
				int lw = MEquityUtil.updateEquity(meqs, Equity.HILO_AFLO8_HALF, lovals, null, 0);
				if (hw >= 0 && hw == lw) {
					meqs[hw].scoopcount++;
				}
				
			} else {
				// high winner
				int hw = MEquityUtil.updateEquity(meqs, Equity.HI_ONLY, hivals, null, 0);
				if (hw >= 0) {
					meqs[hw].scoopcount++;
				}
			}
		}

		MEquityUtil.summariseEquity(meqs, count, hiloCount);
		// TODO
		//MEquityUtil.summariseOuts(meqs, k);
		return meqs;
	}

	/**
	 * Calculate value of holdem/omaha hand (using at least min cards from hand). 
	 */
	private int heValue(final Value v, final String[] board, final String[] hole) {
		int hv = 0;
		for (int n = min; n <= 2; n++) {
			final int nh = MathsUtil.bincoff(hole.length, n);
			final int nb = MathsUtil.bincoff(board.length, 5 - n);
			for (int kh = 0; kh < nh; kh++) {
				MathsUtil.kcomb(n, kh, hole, valueTemp, 0);
				for (int kb = 0; kb < nb; kb++) {
					MathsUtil.kcomb(5 - n, kb, board, valueTemp, n);
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
