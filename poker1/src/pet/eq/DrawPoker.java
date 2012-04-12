package pet.eq;

import java.util.Arrays;

/**
 * Draw poker equity methods
 */
public class DrawPoker extends Poker {
	
	@Override
	public HandEq[] equity(String[] board, String[][] hands) {
		return equity(hands);
	}

	/**
	 * Calculate draw equity using random remaining cards.
	 * (Exact equity using combinatorials is too hard with more than 2 blank cards).
	 */
	public HandEq[] equity(String[][] hands) {
		System.out.println("draw sample equity: " + Arrays.deepToString(hands));
		final String[] d = ArrayUtil.remove(Poker.FULL_DECK, null, hands);
		final HandEq[] eqs = HandEq.makeHandEqs(hands.length, d.length, false);
		RandomUtil.shuffle(d);
		final String[] h = new String[5];
		
		// hand values for a particular board
		final int[] vals = new int[hands.length];
		final int c = 250000;
		final long[] t = new long[1];
		
		for (int p = 0; p < c; p++) {
			t[0] = 0;
			for (int hn = 0; hn < hands.length; hn++) {
				// could be any length
				String[] hand = hands[hn];
				for (int n = 0; n < 5; n++) {
					if (hand.length > n) {
						h[n] = hand[n];
					} else {
						h[n] = RandomUtil.pick(d, t);
					}
				}
				int v = value(h);
				vals[hn] = v;
			}
			HandEq.updateEquities(eqs, vals);
		}
		HandEq.summariseEquities(eqs, c);
		return eqs;
	}
	
	@Override
	public int value(String[] board, String[] hole) {
		if (board != null || hole.length != 5) {
			throw new RuntimeException("invalid draw hand " + Arrays.toString(hole));
		}
		return value(hole);
	}
	
}
