package pet.hp.state;

import java.util.*;

import pet.eq.*;
import pet.hp.*;

public class CardsStateUtil {
	
	/**
	 * get the hole cards the current player kept and discarded
	 */
	private static void kept(String[] hole1, String[] hole2, String[] in, String[] out) {
		int i = 0, j = 0;
		for (int n1 = 0; n1 < 5; n1++) {
			find: {
				for (int n2 = 0; n2 < 5; n2++) {
					if (hole1[n1].equals(hole2[n2])) {
						in[i++] = hole1[n1];
						break find;
					}
				}
				out[j++] = hole1[n1];
			}
		}
	}
	
	/**
	 * Get cards player had on this street
	 */
	public static CardsState getCards(final Hand hand, final Seat seat, final int streetIndex) {
		if (seat.finalHoleCards == null && seat.finalUpCards == null) {
			// nothing to base return value on
			// note that only stud has up cards and the hole cards could be null
			return null;
		}
		
		CardsState cs = null;
		
		boolean high = false;
		switch (hand.game.type) {
			case Game.STUD_TYPE:
			case Game.STUDHL_TYPE:
			case Game.RAZZ_TYPE:
				// XXX should maybe emphasise difference between down and up cards
				String[] cards = HandUtil.getFinalCards(hand.game.type, seat);
				if (streetIndex < 4) {
					cards = Arrays.copyOf(cards, streetIndex + 3);
				}
				// don't sort (though could sort first two)
				return new CardsState(cards, null, false, null);
			
			case Game.FCD_TYPE:
				high = true;
				
			case Game.DSTD_TYPE:
			case Game.DSSD_TYPE:
				if (streetIndex == GameUtil.getMaxStreets(hand.game.type) - 1) {
					// on final street just return final hand from seat
					cs = new CardsState(seat.finalHoleCards.clone(), null, false, null);
					
				} else if (hand.myseat == seat) {
					// get current player cards but also see which ones were kept
					String[] x = hand.myDrawCards(streetIndex);
					if (x == null) {
						return null;
					}
					System.out.println("my hole cards for street " + streetIndex + " are " + Arrays.toString(x));
					
					String[] y = hand.myDrawCards(streetIndex + 1);
					if (y == null) {
						y = hand.myseat.finalHoleCards;
					}
					System.out.println("my hole cards for street " + (streetIndex+1) + " are " + Arrays.toString(y));
					
					final ArrayList<DrawPoker.Draw> l = new ArrayList<DrawPoker.Draw>();
					final int drawn = seat.drawn(streetIndex);
					DrawPoker.getDrawingHand(l, x, drawn, high);
					final String[] k = new String[5 - drawn];
					final String[] d = new String[drawn];
					kept(x, y, k, d);
					cs = new CardsState(k, d, false, l);
					
				} else {
					// guess opponents hole cards based on final hand
					final ArrayList<DrawPoker.Draw> l = new ArrayList<DrawPoker.Draw>();
					final int drawn = seat.drawn(streetIndex);
					String[] h = DrawPoker.getDrawingHand(l, seat.finalHoleCards, drawn, high);
					cs = new CardsState(h, null, true, l);
				}
				break;
				
			case Game.HE_TYPE:
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
				cs = new CardsState(seat.finalHoleCards, null, false, null);
				break;
				
			default:
				throw new RuntimeException("unknown game type " + hand.game);
		}
		
		Arrays.sort(cs.cards, Cmp.revCardCmp);
		if (cs.discarded != null) {
			Arrays.sort(cs.discarded, Cmp.revCardCmp);
		}
		return cs;
	}
	
}
