package pet.eq;

import java.util.Arrays;
import java.util.Random;

/**
 * Draw poker equity methods
 */
public class DrawPoker extends Poker {
	
	/**
	 * Calculate draw equity using random remaining cards.
	 * (Exact equity using combinatorials is too hard with more than 2 blank cards).
	 */
	private static MEquity[] equityImpl(Value value, String[][] holeCards, String[] blockers, int draws) {
		System.out.println("draw sample equity: " + Arrays.deepToString(holeCards) + " draws " + draws);
		if (draws < 0 || draws > 3) {
			throw new RuntimeException("invalid draws: " + draws);
		}
		
		// remaining cards in deck
		final String[] deck = Poker.remdeck(holeCards, blockers);
		
		// return value
		final MEquity[] meqs = MEquityUtil.makeMEquity(holeCards.length, false, value.eqtype(), deck.length, false);
		
		// get current hand values (not equity)
		final int[] vals = new int[holeCards.length];
		for (int n = 0; n < holeCards.length; n++) {
			if (holeCards[n].length == 5) {
				vals[n] = value.value(holeCards[n]);
			}
		}
		MEquityUtil.updateCurrent(meqs, value.eqtype(), vals);
		
		if (draws == 0) {
			// final street, just return current values
			System.out.println("no draws, using current");
			int hw = MEquityUtil.updateEquity(meqs, value.eqtype(), vals, null);
			if (hw >= 0) {
				meqs[hw].scoopcount++;
			}
			MEquityUtil.summariseEquity(meqs, 1, 0);
			
		} else {
			// draw at least once
			final String[] hand = new String[5];
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
						for (int n = 0; n < 5; n++) {
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
				int hw = MEquityUtil.updateEquity(meqs, value.eqtype(), vals, null);
				if (hw >= 0) {
					meqs[hw].scoopcount++;
				}
			}
			MEquityUtil.summariseEquity(meqs, samples, 0);
		}
		
		return meqs;
	}
	
	/**
	 * Guess the players drawing hand.
	 * Always returns new array.
	 */
	public static String[] getDrawingHand(String[] hand, int drawn) {
		switch (drawn) {
			case 0:
				// stand pat
				return hand.clone();
			case 1:
			case 2:
				// drawing at something
				return getDraw(hand, drawn);
			case 3:
				// if pair, return pair, otherwise high cards
				return getPair(hand);
			case 4: {
				// keep high card
				return getHighCard(hand);
			}
			case 5:
				// discard all
				return new String[0];
			default:
				throw new RuntimeException("invalid drawn " + drawn);
		}
	}
	
	/**
	 * get the high card in the hand.
	 * always returns new array
	 */
	private static String[] getHighCard(String[] hand) {
		String[] a = hand.clone();
		Arrays.sort(a, Cmp.revCardCmp);
		return new String[] { a[0] };
	}
	
	/**
	 * get best two cards in hand.
	 * always returns new array
	 */
	private static String[] getPair(final String[] hand) {
		String[] h = hand.clone();
		Arrays.sort(h, Cmp.revCardCmp);
		for (int n = 1; n < h.length; n++) {
			if (faceToValue(h[n-1], true) == faceToValue(h[n], true)) {
				// return highest pair
				return new String[] { h[n-1], h[n] };
			}
		}
		// return high cards
		return new String[] { h[0], h[1] };
	}
	
	/**
	 * Get the trips/st/fl draw by brute force
	 * FIXME
	 * draw 1 with oesd and higher gutty -> assume oesd, not gutty
	 * draw 2 with 3-flush and 3-broad -> assume high cards, not back door flush
	 * draw 2 with 3-str -> keep 3-str not higher back door gutty
	 */
	public static String[] getDraw(final String[] hand, final int drawn) {
		// from players point of view, all other cards are possible
		final String[] deck = Poker.remdeck(null, hand);
		final String[] h = new String[5];
		final String[] maxh = new String[5 - drawn];
		final int pmax = MathsUtil.bincoff(5, 5 - drawn);
		final int qmax = MathsUtil.bincoff(deck.length, drawn);
		int maxv = 0;
		
		for (int p = 0; p < pmax; p++) {
			// pick kept from hand
			MathsUtil.kcomb(5 - drawn, p, hand, h, 0);
			for (int q = 0; q < qmax; q++) {
				// pick drawn from deck
				MathsUtil.kcomb(drawn, q, deck, h, 5 - drawn);
				int v = value(h);
				// ignore draws to straight flush...
				if (v > maxv && v < SF_MASK) {
					// copy new max hand
					for (int n = 0; n < 5 - drawn; n++) {
						maxh[n] = h[n];
					}
					maxv = v;
				}
			}
		}
		
		//System.out.println("hand " + Arrays.toString(maxh) + " => " + valueString(maxv));
		return maxh;
	}
	
	//
	// instance methods
	//
	
	private final boolean high;
	
	public DrawPoker(boolean high) {
		this.high = high;
	}
	
	@Override
	public synchronized MEquity[] equity(String[] board, String[][] hands, String[] blockers, int draws) {
		if (board != null) {
			throw new RuntimeException("invalid board: " + Arrays.toString(board));
		}
		return equityImpl(high ? Value.hiValue : Value.dsLowValue, hands, blockers, draws);
	}
	
	@Override
	public int value(String[] board, String[] hole) {
		if (board != null || hole.length != 5) {
			throw new RuntimeException("invalid draw hand " + Arrays.toString(hole));
		}
		return value(hole);
	}
	
}
