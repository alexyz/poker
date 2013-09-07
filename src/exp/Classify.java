
package exp;

import java.util.*;

import pet.eq.*;
import pet.eq.impl.*;

/**
 * experimental class to classify straight draws
 */
public class Classify {
	public static void main(String[] args) {
		// generate random hand
		// call classify
		String[] fulldeck = Poker.deck();
		Random r = new Random();
		
		for (int n = 0; n < 100; n++) {
			ArrayUtil.shuffle(fulldeck, r);
			String[] hole = Arrays.copyOfRange(fulldeck, 0, 4);
			String[] flopBoard = Arrays.copyOfRange(fulldeck, 4, 7);
			
			Poker p = new HEPoker(true, false);
			int fv = p.value(flopBoard, hole);
			
			String str = descstr(flopBoard, hole);
			if (str.length() > 0) {
				System.out.println(PokerUtil.cardsString(hole) + " + " + PokerUtil.cardsString(flopBoard) + " => " + Poker.valueString(fv));
				System.out.println("straight description: " + str);
			}
		}
		
		// [nut/non nut] [inside str draw/open ended str draw/n-card [nut] wrap] [(outs: m, nut outs: o)]
		// fd: tainted
		// bp: tainted, no nut outs
		// 4567 [56k] = tp, oesd
	}
	
	// call this if <= str
	private static String descstr(String[] board, String[] hole) {
		// return outs
		// return nut outs
		// return desc {nut str, non nut str}
		// { wrap, oesd }
		final String[][] holes = new String[][] { hole };
		final int ns = getnutstr(holes, board);
		//System.out.println("nut str is " + Poker.valueString(ns));
		
		String ret = "";
		
		final Poker p = new HEPoker(true, false, Value.strHiValue, null);
		final int v = p.value(board, hole);
		if (v > 0) {
			ret += Poker.valueString(v);
			if (v == ns) {
				ret += " (nut)";
			} else {
				ret += " (non nut)";
			}
		}
		
		if (v == NS) {
			return ret;
		}
		
		// for each card in deck, does it make higher str
		final String[] deck = Poker.remdeck(null, board, hole);
		final String[] board2 = Arrays.copyOf(board, board.length + 1);
		final List<String> outs = new ArrayList<>();
		final List<String> nutouts = new ArrayList<>();
		for (int n = 0; n < deck.length; n++) {
			board2[board.length] = deck[n];
			final int v2 = p.value(board2, hole);
			if (v2 > v) {
				outs.add(deck[n]);
				final int ns2 = getnutstr(holes, board2);
				if (v2 == ns2) {
					nutouts.add(deck[n]);
				}
			}
		}
		
		if (outs.size() > 0) {
			if (ret.length() > 0) {
				ret += ", ";
			}
			ret += "outs: " + outs.size() + " nut outs: " + nutouts.size();
		}
		return ret;
	}
	
	static final int NS = Poker.ST_RANK | 14;
	
	private static int getnutstr(String[][] holes, String[] board) {
		Poker p = new HEPoker(true, false, Value.strHiValue, null);
		int s = 0;
		for (int n = 0; n < holes.length; n++) {
			s = Math.max(s, p.value(board, holes[n]));
		}
		if (s == NS) {
			return NS;
		}
		String[] deck = Poker.remdeck(holes, board);
		String[] h = new String[2];
		for (int n = 0; n < deck.length; n++) {
			h[0] = deck[n];
			for (int m = n + 1; m < deck.length; m++) {
				h[1] = deck[m];
				int v = p.value(board, h);
				if (v > s) {
					s = v;
				}
				if (s == NS) {
					return NS;
				}
			}
		}
		return s;
	}
}

















