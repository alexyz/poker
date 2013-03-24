package pet.eq;

import java.math.BigInteger;
import java.util.*;

/**
 * Draw poker equity methods
 */
public class DrawPoker extends Poker {
	
	/** represents a possible draw and its average score */
	public static class Draw implements Comparable<Draw> {
		public final String[] cards;
		public double score;
		public Draw(String[] hole, double score) {
			this.cards = hole;
			this.score = score;
		}
		@Override
		public int compareTo(Draw other) {
			return (int) Math.signum(score - other.score);
		}
		@Override
		public String toString() {
			return String.format("%.3f -> ", score) + PokerUtil.cardsString(cards);
		}
	}
	
	//
	// instance methods
	//
	
	private final Value value;
	
	public DrawPoker(boolean high) {
		this.value = high ? Value.hiValue : Value.dsLowValue;
	}
	
	@Override
	public synchronized MEquity[] equity(String[] board, String[][] holeCards, String[] blockers, int draws) {
		System.out.println("draw sample equity: " + Arrays.deepToString(holeCards) + " blockers " + Arrays.toString(blockers) + " draws " + draws);
		if (board.length > 0) {
			throw new RuntimeException("invalid board: " + Arrays.toString(board));
		}
		if (draws < 0 || draws > 3) {
			throw new RuntimeException("invalid draws: " + draws);
		}
		
		// remaining cards in deck
		final String[] deck = Poker.remdeck(holeCards, blockers);
		
		// return value
		final MEquity[] meqs = MEquityUtil.createMEquity(holeCards.length, false, value.eqtype(), deck.length, false);
		
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
			MEquityUtil.updateEquityHi(meqs, value.eqtype(), vals, null);
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
				MEquityUtil.updateEquityHi(meqs, value.eqtype(), vals, null);
			}
			
			MEquityUtil.summariseEquity(meqs, samples, 0);
		}
		
		return meqs;
	}
	
	@Override
	public int value(String[] board, String[] hole) {
		if (board.length > 0 || hole.length != 5) {
			throw new RuntimeException("invalid draw hand " + Arrays.toString(hole));
		}
		return value.value(hole);
	}
	
	/**
	 * get the best drawing hand for the given hand, number drawn and hand valuation.
	 * optionally returns score of all possible drawing hands.
	 */
	public static String[] getDrawingHand(List<DrawPoker.Draw> list, String[] hand, int drawn, boolean high, String[] blockers) {
		System.out.println("get drawing hand: " + Arrays.toString(hand) + " drawn: " + drawn + " blockers: " + Arrays.toString(blockers) + " high: " + high);
		if (hand.length > 5) {
			throw new RuntimeException();
		}
		
		// XXX really should take into account multiple draws
		// but thats only really a problem if you draw a greater number on a
		// later street (which is a bad strategy and almost unheard of)
		// e.g. draw 1, 1, 5 - obviously can't use final hand to predict any of them
		// related problem is that a later hand might contain blockers from a
		// reshuffle and so can't possibly occur on an earlier street
		// and you might not even have enough cards that aren't blocked,
		// i.e. bincoff(length(hand - blockers), 5 - drawn) needs to be >= 1
		if (blockers != null && blockers.length > 0) {
			String[] hand2 = ArrayUtil.sub(hand, blockers);
			if (hand2.length != hand.length) {
				// some of the cards were blocked
				// cheat and increase the draw amount (if necessary)
				drawn = Math.max(5 - hand2.length, drawn);
				hand = hand2;
				System.out.println("hand now: " + Arrays.toString(hand) + " drawn now: " + drawn);
			}
		}
		
		BigInteger combs = MathsUtil.binaryCoefficient(hand.length, 5 - drawn);
		System.out.println("combs: " + combs);
		if (combs.intValue() <= 0) {
			throw new RuntimeException();
		}
		
		// XXX if only 1 comb, just return hand?
		
		// high draw works best with around 0.9, low draw with 0.99
		// generally, you can win in high with any top 10% hand, but low draw
		// pretty much needs 7-high (75432, 76432, 76542, etc) to win
		// XXX actually it probably depends if it's 2-7 single or triple draw
		final double bias = high ? 0.9 : 0.99;
		final Value value = high ? Value.hiValue : Value.dsLowValue;
		
		if (drawn < 0 || drawn > 5) {
			throw new RuntimeException("invalid drawn: " + drawn);
			
		} else if (drawn == 5) {
			// special case, no draw and no meaningful score
			return new String[0];
			
		} else if (drawn == 0) {
			// special case, nothing to test other than given hand
			if (list != null) {
				double s = score(value.value(hand), bias);
				list.add(new DrawPoker.Draw(hand, s));
			}
			return hand.clone();
		}
		
		// drawing 1-4
		
		// from players point of view, all other cards are possible (even the blockers)
		final String[] deck = Poker.remdeck(null, hand);
		final String[] drawnHand = new String[5];
		final int imax = MathsUtil.binaryCoefficientFast(hand.length, 5 - drawn);
		final int jmax = MathsUtil.binaryCoefficientFast(deck.length, drawn);
		System.out.println("imax: " + imax + " jmax: " + jmax);
		
		String[] maxDrawingHand = null;
		float maxScore = -1f;
		
		for (int i = 0; i < imax; i++) {
			Arrays.fill(drawnHand, null);
			// pick kept from hand
			MathsUtil.kCombination(5 - drawn, i, hand, drawnHand, 0);
			//System.out.println("drawnHand: " + Arrays.toString(drawnHand));
			float score = 0;
			
			for (int j = 0; j < jmax; j++) {
				// pick drawn from deck
				MathsUtil.kCombination(drawn, j, deck, drawnHand, 5 - drawn);
				//System.out.println("  drawnHand: " + Arrays.toString(drawnHand));
				int v = value.value(drawnHand);
				score += score(v, bias);
			}
			
			float averageScore = score / (1.0f * jmax);
			String[] drawingHand = Arrays.copyOf(drawnHand, 5 - drawn);
			if (list != null) {
				Arrays.sort(drawingHand, Cmp.revCardCmp);
				list.add(new DrawPoker.Draw(drawingHand, averageScore));
			}
			
			if (score > maxScore) {
				// copy new max hole cards
				maxDrawingHand = drawingHand;
				maxScore = score;
			}
		}
		
		if (list != null) {
			Collections.sort(list);
			Collections.reverse(list);
		}
		return maxDrawingHand;
	}

	/**
	 * get normalised score of hand (i.e. hand value is 0-1), optionally
	 * inverted. bias is 0.5 to 1, representing how many values are less than
	 * 0.5, e.g. 0.9 means 90% of values are less than 0.5
	 */
	protected static double score(final int value, final double bias) {
		if (bias < 0.5 || bias > 1.0) {
			throw new RuntimeException("invalid bias " + bias);
		}
		
		// get high value
		final boolean high;
		final int highValue;
		switch (value & TYPE) {
			case HI_TYPE:
				high = true;
				highValue = value;
				break;
			case DS_LOW_TYPE:
				high = false;
				highValue = MAX_MASK - (value & HAND);
				break;
			default:
				// ace to five doesn't include str/fl
				// but then, no drawing games use ace to five values so doesn't matter
				throw new RuntimeException("can't get score of " + Poker.valueString(value));
		}
		
		final int[] highValues = highValues();
		int p = Arrays.binarySearch(highValues, highValue);
		if (p < 0) {
			throw new RuntimeException("not a high value: " + Poker.valueString(highValue));
		}
		
		if (!high) {
			// invert score for deuce to seven low
			p = highValues.length - 1 - p;
		}
		
		// raise score to some power to bias toward high values
		// note: for k=x^y, y=log(k)/log(x)... i think
		return Math.pow((1f * p) / (highValues.length - 1f), Math.log(0.5) / Math.log(bias));
	}
	
}
