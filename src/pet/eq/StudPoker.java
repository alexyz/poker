package pet.eq;

import java.util.Arrays;
import java.util.Random;

/**
 * stud and razz poker calculations
 */
public class StudPoker extends Poker {

	private final String[] tempHand = new String[5];
	private final String[] tempHoleCards = new String[7];
	private final Value value;
	private final boolean hilo;
	
	public StudPoker(Value value, boolean hilo) {
		this.value = value;
		this.hilo = hilo;
	}

	/** 
	 * passes 3-7 card hands and convert 6+1 card hands into 7 card hands
	 */
	private String[] normalise(final String[] board, final String[] holeCards, final boolean create) {
		// validate
		if (board != null && (board.length > 1 || (board.length == 1 && holeCards.length != 6))) {
			throw new RuntimeException("invalid board");
		}
		
		if (holeCards.length < 3 || holeCards.length > 7) {
			throw new RuntimeException("invalid hand");
		}
		
		if (board != null && board.length > 0) {
			// join board and hand
			final String[] newHoleCards = create ? new String[7] : tempHoleCards;
			for (int n = 0; n < holeCards.length; n++) {
				newHoleCards[n] = holeCards[n];
			}
			newHoleCards[6] = board[0];
			return newHoleCards;
			
		} else {
			return holeCards;
		}
	}
	
	/**
	 * return value of 7 card stud hand
	 */
	private int studValue(final Value val, final String[] hand) {
		int maxv = 0;
		if (hand.length >= 5) {
			// pick best 5 card hand
			final int positions = MathsUtil.bincoff(hand.length, 5);
			for (int p = 0; p < positions; p++) {
				MathsUtil.kcomb(5, p, hand, tempHand, 0);
				final int v = val.value(tempHand);
				if (v > maxv) {
					maxv = v;
				}
			}
		}
		return maxv;
	}
	
	@Override
	public synchronized MEquity[] equity(final String[] board, final String[][] holeCardsOrig, final String[] blockers) {
		System.out.println("stud sample equity: " + Arrays.deepToString(holeCardsOrig) + " board " + Arrays.toString(board) + " blockers " + Arrays.toString(blockers));

		// remaining cards in deck
		// use original cards so none are duplicated
		final String[] deck = Poker.remdeck(holeCardsOrig, blockers, board);
		
		// normalise hands
		final String[][] holeCards = new String[holeCardsOrig.length][];
		for (int n = 0; n < holeCardsOrig.length; n++) {
			holeCards[n] = normalise(board, holeCardsOrig[n], true);
		}

		// return value
		final MEquity[] meqs = MEquityUtil.makeMEquity(holeCards.length, hilo, value.eqtype(), deck.length, false);

		// get current hand values (not equity)
		// note that "hi" doesn't necessarily mean high value, just non-hi/lo value
		final int[] hivals = new int[holeCards.length];
		final int[] lovals = hilo ? new int[holeCards.length] : null;
		
		// get current values
		for (int n = 0; n < holeCards.length; n++) {
			if (holeCards[n].length >= 5) {
				hivals[n] = studValue(value, holeCards[n]);
				if (hilo) {
					hivals[n] = studValue(Value.afLow8Value, holeCards[n]);
				}
			}
		}
		
		// set current values
		MEquityUtil.updateCurrent(meqs, value.eqtype(), hivals);
		if (hilo) {
			MEquityUtil.updateCurrent(meqs, Equity.HILO_HI_HALF, hivals);
			MEquityUtil.updateCurrent(meqs, Equity.HILO_AFLO8_HALF, lovals);
		}
		
		final Random r = new Random();
		final int maxn = 10000;
		int hiloCount = 0;
		
		for (int n = 0; n < maxn; n++) {
			ArrayUtil.shuffle(deck, r);
			int di = 0;
			String commCard = null;
			if (holeCards.length >= 8) {
				commCard = deck[di++];
			}
			boolean hasLow = false;
			for (int h = 0; h < holeCards.length; h++) {
				// could be any length
				String[] hc = holeCards[h];
				for (int c = 0; c < 7; c++) {
					if (hc.length > c) {
						tempHoleCards[c] = hc[c];
					} else if (c < 6 || commCard == null) {
						tempHoleCards[c] = deck[di++];
					} else {
						tempHoleCards[c] = commCard;
					}
				}
				hivals[h] = studValue(value, tempHoleCards);
				if (hilo) {
					int lv = studValue(Value.afLow8Value, tempHoleCards);
					if (lv > 0) {
						lovals[h] = lv;
						hasLow = true;
					}
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
				// high winner only
				int hw = MEquityUtil.updateEquity(meqs, value.eqtype(), hivals, null, 0);
				if (hw >= 0) {
					meqs[hw].scoopcount++;
				}
			}
		}

		MEquityUtil.summariseEquity(meqs, maxn, hiloCount);
		return meqs;
	}
	
	@Override
	public int value(String[] board, String[] cards) {
		// only does one value type...
		if (cards.length >= 5) {
			return studValue(value, normalise(board, cards, false));
		} else {
			return 0;
		}
	}
	
}
