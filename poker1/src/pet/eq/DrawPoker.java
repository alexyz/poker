package pet.eq;

/**
 * Draw poker equity methods
 */
public class DPoker extends Poker {

	/**
	 * Calculate draw equity using random remaining cards.
	 * (Exact equity is too hard with more than 2 blank cards).
	 */
	public HandEq[] deqs(String[][] hands) {
		final String[] d = Util.remdeck(Poker.FULL_DECK, null, hands);
		final HandEq[] eqs = HandEq.makeHandEqs(hands.length, d.length, false);
		Util.shuffle(d);
		final String[] h = new String[5];
		
		// hand values for a particular board
		final int[] vals = new int[hands.length];
		final int c = 250000;
		final long[] t = new long[1];
		
		for (int p = 0; p < c; p++) {
			t[0] = 0;
			for (int hn = 0; hn < hands.length; hn++) {
				String[] hand = hands[hn];
				for (int n = 0; n < 5; n++) {
					if (hand[n] != null) {
						h[n] = hand[n];
					} else {
						h[n] = Util.pick(d, t);
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
	
}
