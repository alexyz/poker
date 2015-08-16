package pet.eq;

import java.math.BigInteger;
import java.util.*;

import static pet.eq.Poker.*;

public class DrawPrediction {

	/**
	 * get the best drawing hand for the given hand, number drawn and hand valuation.
	 * optionally returns score of all possible drawing hands.
	 */
	public static String[] getDrawingHand(List<Draw> list, String[] hand, int drawn, boolean high, String[] blockers) {
		System.out.println("get drawing hand: " + Arrays.toString(hand) + " drawn: " + drawn + " blockers: " + Arrays.toString(blockers) + " high: " + high);
		if (hand.length > 5) {
			throw new RuntimeException("invalid hand: " + Arrays.toString(hand));
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
		
		BigInteger combs = MathsUtil.binomialCoefficient(hand.length, 5 - drawn);
		System.out.println("combs: " + combs);
		if (combs.intValue() <= 0) {
			throw new RuntimeException("invalid combs: " + combs);
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
				float s = score(value.value(hand), bias);
				list.add(new Draw(hand, s));
			}
			return hand.clone();
		}
		
		// drawing 1-4
		
		// from players point of view, all other cards are possible (even the blockers)
		final String[] deck = Poker.remdeck(null, hand);
		final String[] drawnHand = new String[5];
		final int imax = MathsUtil.binomialCoefficientFast(hand.length, 5 - drawn);
		final int jmax = MathsUtil.binomialCoefficientFast(deck.length, drawn);
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
				list.add(new Draw(drawingHand, averageScore));
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
	protected static float score(final int value, final double bias) {
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
				highValue = HI_TYPE | (MAX_RANK - (value & HAND));
				break;
			default:
				// ace to five doesn't include str/fl
				// but then, no drawing games use ace to five values so doesn't matter
				throw new RuntimeException("can't get score of " + Integer.toHexString(value));
		}
		
		final int[] highValues = highValues();
		int p = Arrays.binarySearch(highValues, highValue);
		if (p < 0) {
			throw new RuntimeException("not a high value: " + Integer.toHexString(highValue));
		}
		
		if (!high) {
			// invert score for deuce to seven low
			p = highValues.length - 1 - p;
		}
		
		// raise score to some power to bias toward high values
		// note: for k=x^y, y=log(k)/log(x)... i think
		return (float) Math.pow((1f * p) / (highValues.length - 1f), Math.log(0.5) / Math.log(bias));
	}
	
}
