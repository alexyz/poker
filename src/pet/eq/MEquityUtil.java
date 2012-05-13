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
	static void updateCurrent(MEquity[] meqs, int eqtype, int[] vals) {
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
			Equity e = meqs[i].eq[eqtype];
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
	 * given cards.
	 * Return single winner, if any, or -1
	 */
	static int updateEquity(MEquity[] meqs, int eqtype, int[] vals, String[] cards, int off) {
		// find highest hand and number of times it occurs
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
		
		int winner = -1;
		
		for (int i = 0; i < vals.length; i++) {
			if (vals[i] == max) {
				// update the win/tied/rank count
				Equity e = meqs[i].eq[eqtype];
				if (maxcount == 1) {
					// update mask
					winner = i;
					e.woncount++;
				} else {
					e.tiedcount++;
					e.tiedwithcount += maxcount;
				}
				if (max < Poker.LOW_MASK) {
					// FIXME hi only
					e.wonrankcount[Poker.rank(max)]++;
				}
				
				// FIXME need to do this for mequity, not equity
				// count the cards as outs if this turns losing hand into
				// win/tie or tying hand into win
				/*
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
				*/
			}
		}
		
		return winner;
	}
	
	/**
	 * summarise equities (convert counts to percentages)
	 */
	static void summariseEquity(MEquity[] meqs, int count, int hiloCount) {
		System.out.println("summarise count=" + count + " hilocount=" + hiloCount);
		for (MEquity meq : meqs) {
			System.out.println("meq " + meq);
			
			Equity hionly = meq.hionly();
			hionly.summariseEquity(count);
			System.out.println("  hionly won: " + hionly.won + " tied: " + hionly.tied + " total: " + hionly.total);
			
			if (hiloCount == 0) {
				meq.totaleq = hionly.total;
				
			} else {
				Equity hihalf = meq.hihalf();
				// high count as it applies to every hand not just hi/lo hands
				hihalf.summariseEquity(count);
				System.out.println("  hihalf won: " + hihalf.won + " tied: " + hihalf.tied + " total: " + hihalf.total);
				System.out.println("  hionly+hihalf won: " + (hionly.won + hihalf.won) + " tied: " + (hionly.tied+hihalf.tied) + " total: " + (hionly.total+hihalf.total));
				
				Equity lohalf = meq.lohalf();
				lohalf.summariseEquity(count);
				System.out.println("  lohalf won: " + lohalf.won + " tied: " + lohalf.tied + " total: " + lohalf.total);
				
				meq.lowPossible = (hiloCount * 100f) / count;
				System.out.println("  low possible: " + meq.lowPossible);
				
				meq.totaleq = hionly.total + (hihalf.total + lohalf.total) / 2;
			}
			System.out.println("  total eq: " + meq.totaleq);
			
			meq.scoop = (meq.scoopcount * 100f) / count;
			System.out.println("  scoop count: " + meq.scoopcount + " scoop: " + meq.scoop);
		}
	}

	static void summariseOuts(MEquity[] meqs, int k) {
		for (MEquity meq : meqs) {
			for (Equity eq : meq.eq) {
				eq.summariseOuts(meq.remCards, k);
			}
		}
	}

	/**
	 * Return string representing current value of hand
	 */
	public static String currentString(MEquity me) {
		String s = Poker.valueString(me.hionly().current);
		if (me.hilo()) {
			s += " / " + Poker.valueString(me.lohalf().current);
		}
		return s;
	}

	/**
	 * Return string representing current equity of hand
	 * TODO short equity string, just use total
	 */
	public static String equityString(MEquity me) {
		String s;
		Equity hionly = me.hionly();
		
		if (me.hilo()) {
			Equity hihalf = me.hihalf();
			Equity lohalf = me.lohalf();
			s = String.format("%.1f-%.1f-%.1f%%", hionly, hihalf, lohalf);
			if (hionly.tied + hihalf.tied + lohalf.tied != 0) {
				s += String.format(" (%.0f-%.0f-%.0f T)", hionly.tied, hihalf.tied, lohalf.tied);
			}
			
		} else {
			s = String.format("%.1f%%", hionly.won);
			if (hionly.tied != 0) {
				s += String.format(" (%.1f%% T)", hionly.tied);
			}
		}
		
		return s;
	}
	
}
