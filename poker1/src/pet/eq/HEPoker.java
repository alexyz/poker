package pet.eq;

import static pet.eq.MathsUtil.*;
import java.util.Arrays;

/**
 * Holdem and Omaha hand analysis, including a combinatorial number system.
 * TODO add hilo
 */
public class HEPoker extends Poker {

	private final boolean omaha;

	/**
	 * create equity calculator for given game type
	 */
	public HEPoker(boolean omaha) {
		this.omaha = omaha;
	}

	private final String[] hand = new String[5];

	/**
	 * Calculate holdem/omaha equity.
	 * Preflop uses random sample boards.
	 * Flop and later uses every possible combination of remaining cards.
	 */
	@Override
	public HandEq[] equity(String[] board, String[][] holes) {
		/*
		for (String[] hand : holes) {
			validateHand(hand, board, omaha);
		}
		*/
		return board != null && board.length >= 3 ? exactEquity(board, holes) : sampleEquity(holes);
	}

	/**
	 * Calc exact tex/omaha hand equity for each hand for given flop (can include turn and riv)
	 */
	private HandEq[] exactEquity(String[] flop, String[][] holes) {
		//println("flop size: " + flop.length);

		// get current values
		final int[] vals = new int[holes.length];
		for (int n = 0; n < holes.length; n++) {
			vals[n] = value(flop, holes[n]);
		}

		final String[] deck = ArrayUtil.remove(Poker.FULL_DECK, flop, holes);
		//println("cards remaining: " + deck.length);

		final HandEq[] eqs = HandEq.makeHandEqs(holes.length, deck.length, true);
		HandEq.updateCurrent(eqs, vals);

		// get equity
		final String[] board = Arrays.copyOf(flop, 5);
		final int k = 5 - flop.length;
		//println("cards to deal: " + k);
		final int combs = bincoff(deck.length, k);
		//println("combinations remaining: " + combs);
		for (int p = 0; p < combs; p++) {
			kcomb(k, p, deck, board, flop.length);
			for (int i = 0; i < holes.length; i++) {
				vals[i] = value(board, holes[i]);
			}
			HandEq.updateEquities(eqs, vals, board, flop.length);
		}

		HandEq.summariseEquities(eqs, combs);
		HandEq.summariseOuts(eqs, k);
		return eqs;
	}

	/**
	 * Calc sampled tex/omaha hand equity for each hand.
	 */
	private HandEq[] sampleEquity(String[][] holes) {
		final String[] deck = ArrayUtil.remove(Poker.FULL_DECK, null, holes);
		final String[] board = new String[5];
		final HandEq[] eqs = HandEq.makeHandEqs(holes.length, deck.length, false);

		// hand values for a particular board
		final int[] vals = new int[holes.length];
		final long[] picked = new long[1];
		final int sz = 1000;

		for (int p = 0; p < sz; p++) {
			picked[0] = 0;
			for (int n = 0; n < 5; n++) {
				board[n] = RandomUtil.pick(deck, picked);
			}
			for (int i = 0; i < holes.length; i++) {
				vals[i] = value(board, holes[i]);
			}
			HandEq.updateEquities(eqs, vals);
		}

		HandEq.summariseEquities(eqs, sz);
		return eqs;
	}

	private static void validateHand(String[] hole, String[] board, boolean omaha) {
		if (board == null || board.length < 3 || board.length > 5) {
			throw new RuntimeException("invalid board " + Arrays.toString(board));
		}
		if (omaha) {
			// allow two or thre hole cards
			if (hole.length < 2 || hole.length > 4) {
				throw new RuntimeException("invalid omaha hand " + Arrays.toString(hole));
			}
		} else {
			// allow one hole card
			if (hole.length < 1 || hole.length > 2 || board.length + hole.length < 5) {
				throw new RuntimeException("invalid holdem hand " + Arrays.toString(hole));
			}
		}
	}

	/**
	 * Calculate value of holdem/omaha hand (must be at least 5 cards in total, and for omaha, at least 2 hole cards)
	 */
	@Override
	public int value(String[] board, String[] hole) {
		validateHand(hole, board, omaha);
		//System.out.println("value(" + Arrays.asList(hole) +"," + Arrays.asList(board) +","+omaha+")");
		final int min = omaha ? 2 : 0;
		int hv = 0;
		for (int n = min; n <= 2; n++) {
			final int nh = bincoff(hole.length, n);
			final int nb = bincoff(board.length, 5 - n);
			for (int kh = 0; kh < nh; kh++) {
				kcomb(n, kh, hole, hand, 0);
				for (int kb = 0; kb < nb; kb++) {
					kcomb(5 - n, kb, board, hand, n);
					final int v = value(hand);
					//System.out.println(Arrays.asList(h5) + " - " + Poker.desc(v));
					if (v > hv) {
						hv = v;
					}
				}
			}
		}
		return hv;
	}

}
