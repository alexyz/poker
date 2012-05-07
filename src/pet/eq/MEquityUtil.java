package pet.eq;

public class MEquityUtil {
	
	/**
	 * Make array of multiple hand equities for given number of remaining cards,
	 * game type and calculation method
	 */
	static MEquity[] makeMEquity(int hands, boolean hilo, int rem, boolean exact) {
		MEquity[] meqs = new MEquity[hands];
		for (int n = 0; n < meqs.length; n++) {
			meqs[n] = new MEquity(hilo, rem, exact);
		}
		return meqs;
	}

	/**
	 * Set the current value of the hands, not the equity
	 */
	public static void updateCurrent(MEquity[] meqs, boolean hi, int[] vals) {
		int max = 0, times = 0;
		for (int i = 0; i < vals.length; i++) {
			int v = vals[i];
			if (v > max) {
				max = v;
				times = 1;
			} else if (v == max) {
				times++;
			}
		}
		for (int i = 0; i < vals.length; i++) {
			Equity e = hi ? meqs[i].hi : meqs[i].lo;
			e.current = vals[i];
			if (e.current == max) {
				if (times == 1) {
					e.curwin = true;
				} else {
					e.curtie = true;
				}
			}
		}
	}

	/**
	 * Update equities win, tie and win rank with given hand values for the
	 * given cards
	 */
	static void updateEquity(MEquity[] meqs, boolean hi, int[] vals, String[] cards, int off) {
		int max = 0, maxcount = 0;
		for (int i = 0; i < vals.length; i++) {
			int v = vals[i];
			if (v > max) {
				max = v;
				maxcount = 1;
			} else if (v == max) {
				maxcount++;
			}
		}
		for (int i = 0; i < vals.length; i++) {
			if (vals[i] == max) {
				Equity e = hi ? meqs[i].hi : meqs[i].lo;
				if (maxcount == 1) {
					e.woncount++;
				} else {
					e.tiedcount++;
				}
				if (hi) {
					// FIXME hi only
					e.wonrankcount[Poker.rank(max)]++;
				}

				// count the cards as outs if this turns losing hand into
				// win/tie or tying hand into win
				if (cards != null && (!e.curwin || (e.curtie && maxcount == 1))) {
					for (int c = off; c < cards.length; c++) {
						String card = cards[c];
						int[] count = e.outcount.get(card);
						if (count == null) {
							e.outcount.put(card, count = new int[1]);
						}
						count[0]++;
					}
				}
			}
		}
	}

	/**
	 * summarise equities (convert counts to percentages)
	 */
	static void summariseEquity(MEquity[] meqs, int samples) {
		for (MEquity meq : meqs) {
			meq.hi.summariseEquity(samples);
			if (meq.lo != null) {
				meq.lo.summariseEquity(samples);
			}
		}
	}

	static void summariseOuts(MEquity[] meqs, int k) {
		for (MEquity meq : meqs) {
			meq.hi.summariseOuts(meq.rem, k);
			if (meq.lo != null) {
				meq.lo.summariseOuts(meq.rem, k);
			}
		}
	}

	/**
	 * Return string representing current value of hand
	 */
	public static String currentString(MEquity me) {
		String s = Poker.valueString(me.hi.current);
		if (me.lo != null) {
			s += " / " + Poker.valueString(me.lo.current);
		}
		return s;
	}

	/**
	 * Return string representing current equity of hand
	 */
	public static String equityString(MEquity me) {
		String s;
		if (me.lo == null) {
			s = String.format("%.1f%%", me.hi.won);
			if (me.hi.tied != 0) {
				s += String.format(" (%.1f%% T)", me.hi.tied);
			}
		} else {
			s = String.format("%.1f-%.1f%%", me.hi.won, me.lo.won);
			if (me.hi.tied != 0 || me.lo.tied != 0) {
				s += String.format(" (%.0f-%.0f T)", me.hi.tied, me.lo.tied);
			}
		}
		return s;
	}
	
}
