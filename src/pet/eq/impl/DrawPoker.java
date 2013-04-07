package pet.eq.impl;

import java.util.*;

import pet.eq.*;

/**
 * Draw poker equity methods
 */
public class DrawPoker extends Poker {

	private static void validateHole (String[] hole) {
		if (hole.length < 1 || hole.length > 5) {
			throw new RuntimeException("invalid hole cards: " + Arrays.toString(hole));
		}
	}

	private static void validateBoard (String[] board) {
		if (board != null) {
			throw new RuntimeException("invalid board: " + Arrays.toString(board));
		}
	}
	
	//
	// instance methods
	//
	
	public DrawPoker(Value value) {
		super(value);
	}
	
	@Override
	protected MEquity[] equity(String[] board, String[][] holeCards, String[] blockers, int draws) {
		System.out.println("draw sample equity: " + Arrays.deepToString(holeCards) + " blockers " + Arrays.toString(blockers) + " draws " + draws);
		validateBoard(board);
		if (draws < 0 || draws > 3) {
			throw new RuntimeException("invalid draws: " + draws);
		}
		
		// remaining cards in deck
		final String[] deck = Poker.remdeck(holeCards, blockers);
		
		// return value
		final MEquity[] meqs = MEquityUtil.createMEquities(value.eqtype, holeCards.length, deck.length, false);
		
		// get current hand values (not equity)
		final int[] vals = new int[holeCards.length];
		for (int n = 0; n < holeCards.length; n++) {
			if (holeCards[n].length == value.cards) {
				vals[n] = value.value(holeCards[n]);
			}
		}
		MEquityUtil.updateCurrent(meqs, value.eqtype, vals);
		
		if (draws == 0) {
			// final street, just return current values
			System.out.println("no draws, using current");
			MEquityUtil.updateMEquities(meqs, value.eqtype, vals, null);
			MEquityUtil.summariseMEquities(meqs, 1, 0);
			
		} else {
			// draw at least once
			final String[] hand = new String[value.cards];
			final int samples = 10000;
			final Random r = new Random();
			System.out.println("draw " + draws + ", " + samples + " samples");
			
			for (int s = 0; s < samples; s++) {
				ArrayUtil.shuffle(deck, r);
				int di = 0;
				
				for (int hn = 0; hn < holeCards.length; hn++) {
					int maxv = 0;
					// run the draw multiple times and keep the best
					// this will tend to overestimate equity as players
					// might stand pat on a later draw with a marginal hand
					// XXX if cards length is 5, don't bother with multiple draws
					for (int d = 0; d < draws; d++) {
						// could be any length
						String[] cards = holeCards[hn];
						for (int n = 0; n < value.cards; n++) {
							if (cards.length > n) {
								hand[n] = cards[n];
							} else {
								hand[n] = deck[di];
								// lots of hands and draws might use whole deck
								// ideally should reshuffle but might get same card twice in hand
								// could reshuf for each player
								di = (di + 1) % deck.length;
							}
						}
						int v = value.value(hand);
						if (v > maxv) {
							maxv = v;
						}
					}
					vals[hn] = maxv;
				}
				MEquityUtil.updateMEquities(meqs, value.eqtype, vals, null);
			}
			
			MEquityUtil.summariseMEquities(meqs, samples, 0);
		}
		
		return meqs;
	}
	
	@Override
	public int value(String[] board, String[] hole) {
		validateBoard(board);
		validateHole(hole);
		if (hole.length == value.cards) {
			return value.value(hole);
		} else {
			return 0;
		}
	}
	
}
