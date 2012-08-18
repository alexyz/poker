package pet.hp.state;

import java.util.*;

import pet.eq.*;
import pet.hp.*;

public class CardsStateUtil {
	
	/**
	 * get the hole cards the current player kept and discarded
	 */
	private static CardsState kept(String[] hole1, String[] hole2, int discards) {
		CardsState h = new CardsState(5 - discards, discards);
		int i = 0, j = 0;
		for (int n1 = 0; n1 < 5; n1++) {
			find: {
				for (int n2 = 0; n2 < 5; n2++) {
					if (hole1[n1].equals(hole2[n2])) {
						h.cards[i++] = hole1[n1];
						break find;
					}
				}
				h.discarded[j++] = hole1[n1];
			}
		}
		return h;
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
		
		CardsState hc = null;
		
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
				return new CardsState(cards);
			
			case Game.DSTD_TYPE:
			case Game.DSSD_TYPE:
				if (streetIndex == GameUtil.getMaxStreets(hand.game.type) - 1) {
					// on final street just return final hand from seat
					hc = new CardsState(seat.finalHoleCards.clone());
					
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
					hc = kept(x, y, seat.drawn(streetIndex));
					
				} else {
					// guess opponents hole cards based on final hand
					String[] h = DrawPoker2.getDrawingHand(seat.finalHoleCards, seat.drawn(streetIndex), false);
					hc = new CardsState(h, null, true);
				}
				break;
				
			case Game.FCD_TYPE:
				if (streetIndex == 1) {
					// return final hand
					hc = new CardsState(seat.finalHoleCards.clone());
					
				} else if (hand.myseat == seat) {
					// return starting hand
					hc = kept(hand.myDrawCards0, seat.finalHoleCards, seat.drawn0);
					
				} else {
					// guess what cards the opponent kept
					hc = new CardsState(DrawPoker.getDrawingHand(seat.finalHoleCards, seat.drawn0), null, true);
				}
				break;
				
			case Game.HE_TYPE:
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
				hc = new CardsState(seat.finalHoleCards);
				break;
				
			default:
				throw new RuntimeException("unknown game type " + hand.game);
		}
		
		if (hc != null) {
			Arrays.sort(hc.cards, Cmp.revCardCmp);
			if (hc.discarded != null) {
				Arrays.sort(hc.discarded, Cmp.revCardCmp);
			}
		}
		
		return hc;
	}
	
}
