package pet;

import static pet.MUtil.*;

import java.io.PrintStream;
import java.util.Arrays;

/**
 * Holdem and Omaha hand analysis, including a combinatorial number system.
 * TODO add hilo
 */
public class HEPoker extends Poker {
	
	public static PrintStream out = System.out;
	private final String[] hand = new String[5];
	
	/**
	 * Combinatorial number system.
	 * Get the k combination at position p and write from from into to at offset.
	 */
	private static void kcomb(final int k, int p, final Object[] from, Object[] to, final int off) {
		// for each digit (starting at the last)
		for (int b = k; b >= 1; b--) {
			// find biggest bin coff that will fit p
			for (int a = b - 1; a < 100; a++) {
				int x = bincoff(a, b);
				if (x > p) {
					// this is too big, so the last one must have fit
					p -= bincoff(a - 1, b);
					to[b - 1 + off] = from[a - 1];
					break;
				}
			}
		}
	}

	/**
	 * Calculate holdem/omaha equity.
	 * Preflop uses random sample boards.
	 * Flop and later uses every possible combination of remaining cards.
	 */
	public HandEq[] equity(String[] board, String[][] holes, boolean omaha) {
		for (String[] hand : holes) {
			validateHand(hand, board, omaha);
		}
		return board != null && board.length >= 3 ? exactEquity(board, holes, omaha) : sampleEquity(holes, omaha);
	}

	/**
	 * Calc exact tex/omaha hand equity for each hand for given flop (can include turn and riv)
	 */
	private HandEq[] exactEquity(String[] flop, String[][] holes, boolean omaha) {
		out.println("flop size: " + flop.length);
		
		// get current values
		final int[] vals = new int[holes.length];
		for (int n = 0; n < holes.length; n++) {
			vals[n] = value(holes[n], flop, omaha);
		}
		
		final String[] deck = Util.remdeck(Poker.FULL_DECK, flop, holes);
		out.println("cards remaining: " + deck.length);
		
		final HandEq[] eqs = HandEq.makeHandEqs(holes.length, deck.length, true);
		HandEq.updateCurrent(eqs, vals);
		
		// get equity
		final String[] board = Arrays.copyOf(flop, 5);
		final int k = 5 - flop.length;
		out.println("cards to deal: " + k);
		final int combs = bincoff(deck.length, k);
		out.println("combinations remaining: " + combs);
		for (int p = 0; p < combs; p++) {
			kcomb(k, p, deck, board, flop.length);
			for (int i = 0; i < holes.length; i++) {
				vals[i] = value(holes[i], board, omaha);
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
	 private HandEq[] sampleEquity(String[][] holes, boolean omaha) {
		final String[] deck = Util.remdeck(Poker.FULL_DECK, null, holes);
		final String[] board = new String[5];
		final HandEq[] eqs = HandEq.makeHandEqs(holes.length, deck.length, false);
		
		// hand values for a particular board
		final int[] vals = new int[holes.length];
		final long[] picked = new long[1];
		final int sz = 10000;

		for (int p = 0; p < sz; p++) {
			picked[0] = 0;
			for (int n = 0; n < 5; n++) {
				board[n] = Util.pick(deck, picked);
			}
			for (int i = 0; i < holes.length; i++) {
				vals[i] = value(holes[i], board, omaha);
			}
			HandEq.updateEquities(eqs, vals);
		}

		HandEq.summariseEquities(eqs, sz);
		return eqs;
	}
	 
	 private static void validateHand(String[] hole, String[] board, boolean omaha) {
		 if (board != null && (board.length < 3 || board.length > 5)) {
			 throw new RuntimeException("board length " + board.length);
		 }
		 if (omaha) { 
			 if (hole.length < 2 || hole.length > 4) {
				 throw new RuntimeException("omaha hand length " + hole.length);
			 }
		 } else {
			 if (hole.length > 2 || (board != null && hole.length + board.length < 5)) {
				 throw new RuntimeException("holdem hand length " + hole.length);
			 }
		 }
	 }
	
	/**
	 * Calculate value of holdem/omaha hand (must be at least 5 cards in total, and for omaha, at least 2 hole cards)
	 */
	public int value(String[] hole, String[] board, boolean omaha) {
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
