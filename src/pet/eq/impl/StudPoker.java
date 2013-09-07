package pet.eq.impl;

import java.util.*;

import pet.eq.*;

/**
 * 7 card stud and razz poker calculations
 */
public class StudPoker extends Poker {
	
	private final String[] tempHand = new String[5];
	private final String[] tempHoleCards = new String[7];
	private final boolean hilo;
	
	public StudPoker(Value value, boolean hilo) {
		super(value);
		this.hilo = hilo;
	}
	
	/** 
	 * passes 3-7 card hands and convert 6+1 card hands into 7 card hands
	 */
	private String[] merge(final String[] board, final String[] holeCards, final boolean create) {
		// validate
		if (board != null && board.length > 1) {
			throw new RuntimeException("invalid board");
		}
		
		if (holeCards.length > 7) {
			throw new RuntimeException("invalid hand");
		}
		
		if (board != null && board.length > 0) {
			// join board and hand
			final String[] newHoleCards = create ? new String[holeCards.length + 1] : tempHoleCards;
			for (int n = 0; n < holeCards.length; n++) {
				newHoleCards[n] = holeCards[n];
			}
			newHoleCards[holeCards.length] = board[0];
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
			final int positions = MathsUtil.binomialCoefficientFast(hand.length, 5);
			for (int p = 0; p < positions; p++) {
				MathsUtil.kCombination(5, p, hand, tempHand, 0);
				final int v = val.value(tempHand);
				if (v > maxv) {
					maxv = v;
				}
			}
		}
		return maxv;
	}
	
	@Override
	public synchronized MEquity[] equity(final String[] board, final String[][] holeCardsOrig, final String[] blockers, final int draws) {
		if (draws != 0) {
			throw new RuntimeException("invalid draws: " + draws);
		}
		System.out.println("stud sample equity: " + Arrays.deepToString(holeCardsOrig) + " board " + Arrays.toString(board) + " blockers " + Arrays.toString(blockers));
		
		// note: hole cards may be mixed length
		
		// remaining cards in deck
		// use original cards so none are duplicated
		final String[] deck = Poker.remdeck(holeCardsOrig, blockers, board);
		
		// merge board with hole cards
		final String[][] holeCards = new String[holeCardsOrig.length][];
		int nonblanks = 0;
		if (board != null && board.length > 0) {
			nonblanks++;
		}
		for (int n = 0; n < holeCardsOrig.length; n++) {
			nonblanks += holeCardsOrig[n].length;
			holeCards[n] = merge(board, holeCardsOrig[n], true);
		}
		
		// how many cards do we need to pick
		int blanks;
		if (holeCards.length == 8) {
			// last card will be comm card
			blanks = holeCards.length * 6 + 1 - nonblanks;
		} else {
			blanks = holeCards.length * 7 - nonblanks;
		}
		
		//System.out.println("deck: " + deck.length + " nonblanks: " + nonblanks + " blanks: " + blanks);
		//BigInteger combs = MathsUtil.bincoffslow(deck.length, blanks);
		// XXX if this is less than ~50k, should ideally do exact enumeration of combs and perms 
		// though with 4 or more blanks it tends to be much higher
		//BigInteger combperms = MathsUtil.facslow(blanks).multiply(combs);
		//System.out.println("combs: " + combs + " combperms: " + combperms);
		
		// return value
		final MEquity[] meqs;
		if (hilo) {
			meqs = MEquityUtil.createMEquitiesHL(true, holeCards.length, deck.length, false);
		} else {
			meqs = MEquityUtil.createMEquities(value.eqtype, holeCards.length, deck.length, false);
		}
		
		// get current hand values (not equity)
		// note that "hi" doesn't necessarily mean high value, just non-hi/lo value
		final int[] hivals = new int[holeCards.length];
		final int[] lovals = hilo ? new int[holeCards.length] : null;
		
		// get current values
		boolean hasLow = false;
		for (int n = 0; n < holeCards.length; n++) {
			// returns 0 if less than 5 cards
			hivals[n] = studValue(value, holeCards[n]);
			if (hilo) {
				int lv = studValue(Value.afLow8Value, holeCards[n]);
				lovals[n] = lv;
				if (lv > 0) {
					hasLow = true;
				}
			}
		}
		
		// set current values
		MEquityUtil.updateCurrent(meqs, value.eqtype, hivals);
		if (hasLow) {
			MEquityUtil.updateCurrent(meqs, Equity.Type.HILO_HI_HALF, hivals);
			MEquityUtil.updateCurrent(meqs, Equity.Type.HILO_AFLO8_HALF, lovals);
		}
		
		final int samples;
		// how many hands had lo
		int lowCount = 0;
		
		if (blanks == 0) {
			System.out.println("no blanks, using current values");
			samples = 1;
			
			// no blank cards, just use current as only sample
			if (hasLow) {
				lowCount = 1;
				MEquityUtil.updateMEquitiesHL(meqs, hivals, lovals, null);
				
			} else {
				MEquityUtil.updateMEquities(meqs, value.eqtype, hivals, null);
			}
			
		} else {
			final Random r = new Random();
			samples = 10000;
			System.out.println("blanks: " + blanks + ", using " + samples + " samples");
			
			// sample remaining cards, but exact enumeration might be not be very big
			for (int s = 0; s < samples; s++) {
				// shuffle instead of pick, as stud tends to use most of the deck
				ArrayUtil.shuffle(deck, r);
				int di = 0;
				String commCard = null;
				if (holeCards.length >= 8) {
					commCard = deck[di++];
				}
				hasLow = false;
				
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
						// always assign this as any of them could be low
						lovals[n] = lv;
						if (lv > 0) {
							hasLow = true;
						}
					}
				}
				
				// TODO count outs, but currently can only handle shared (board) outs
				// and with sample, won't be very accurate anyway
				
				if (hasLow) {
					lowCount++;
					MEquityUtil.updateMEquitiesHL(meqs, hivals, lovals, null);
					
				} else {
					// high winner only
					MEquityUtil.updateMEquities(meqs, value.eqtype, hivals, null);
				}
			}
			
		}
		
		MEquityUtil.summariseMEquities(meqs, samples, lowCount);
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
