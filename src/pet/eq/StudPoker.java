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
	private String[] merge(final String[] board, final String[] holeCards, final boolean create) {
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

		// note: hole cards may be mixed length
		
		// remaining cards in deck
		// use original cards so none are duplicated
		final String[] deck = Poker.remdeck(holeCardsOrig, blockers, board);
		
		// merge board with hole cards
		final String[][] holeCards = new String[holeCardsOrig.length][];
		int nonblanks = 0;
		for (int n = 0; n < holeCardsOrig.length; n++) {
			holeCards[n] = merge(board, holeCardsOrig[n], true);
			nonblanks += holeCards[n].length;
		}
		
		int blanks;
		if (holeCards.length == 8) {
			// last card will be comm card
			blanks = holeCards.length * 6 + 1 - nonblanks;
		} else {
			blanks = holeCards.length * 7 - nonblanks;
		}
		System.out.println("deck: " + deck.length + " nonblanks: " + nonblanks + " blanks: " + blanks + " combs: " + MathsUtil.bincoffslow(deck.length, blanks));

		// return value
		final MEquity[] meqs = MEquityUtil.makeMEquity(holeCards.length, hilo, value.eqtype(), deck.length, false);

		// get current hand values (not equity)
		// note that "hi" doesn't necessarily mean high value, just non-hi/lo value
		final int[] hivals = new int[holeCards.length];
		final int[] lovals = hilo ? new int[holeCards.length] : null;
		
		// get current values
		for (int n = 0; n < holeCards.length; n++) {
			// returns 0 if less than 5 cards
			hivals[n] = studValue(value, holeCards[n]);
			if (hilo) {
				lovals[n] = studValue(Value.afLow8Value, holeCards[n]);
			}
		}
		
		// set current values
		MEquityUtil.updateCurrent(meqs, value.eqtype(), hivals);
		if (hilo) {
			MEquityUtil.updateCurrent(meqs, Equity.HILO_HI_HALF, hivals);
			MEquityUtil.updateCurrent(meqs, Equity.HILO_AFLO8_HALF, lovals);
		}
		
		final Random r = new Random();
		final int samples = 10000;
		int hiloCount = 0;
		
		// XXX uses sample remaining cards, but exact enumeration might be not be very big
		for (int s = 0; s < samples; s++) {
			ArrayUtil.shuffle(deck, r);
			int di = 0;
			String commCard = null;
			if (holeCards.length >= 8) {
				commCard = deck[di++];
			}
			boolean hasLow = false;
			
			for (int n = 0; n < holeCards.length; n++) {
				// hole cards could be any length, copy to temp
				for (int c = 0; c < 7; c++) {
					if (holeCards[n].length > c) {
						tempHoleCards[c] = holeCards[n][c];
						
					} else if (commCard == null || c < 6) {
						// pick one from shuffled deck
						tempHoleCards[c] = deck[di++];
						
					} else {
						// use community card for last card
						tempHoleCards[c] = commCard;
					}
				}
				
				hivals[n] = studValue(value, tempHoleCards);
				if (hilo) {
					int lv = studValue(Value.afLow8Value, tempHoleCards);
					if (lv > 0) {
						lovals[n] = lv;
						hasLow = true;
					}
				}
			}

			if (hasLow) {
				hiloCount++;
				// high winner
				int hw = MEquityUtil.updateEquity(meqs, Equity.HILO_HI_HALF, hivals, null);
				// low winner
				int lw = MEquityUtil.updateEquity(meqs, Equity.HILO_AFLO8_HALF, lovals, null);
				if (hw >= 0 && hw == lw) {
					meqs[hw].scoopcount++;
				}
				
			} else {
				// high winner only
				int hw = MEquityUtil.updateEquity(meqs, value.eqtype(), hivals, null);
				if (hw >= 0) {
					meqs[hw].scoopcount++;
				}
			}
		}

		MEquityUtil.summariseEquity(meqs, samples, hiloCount);
		return meqs;
	}
	
	@Override
	public synchronized int value(String[] board, String[] cards) {
		// only does one value type...
		if (cards.length >= 5) {
			return studValue(value, merge(board, cards, false));
		} else {
			return 0;
		}
	}
	
}
