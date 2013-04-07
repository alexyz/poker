package pet.hp.state;

import java.util.*;

import pet.eq.*;
import pet.hp.*;

public class CardsStateUtil {
	
	/**
	 * get the hole cards the current player kept and discarded
	 */
	private static void kept(String[] cards, String[] nextCards, String[] kept, String[] discarded) {
		int i = 0, j = 0;
		for (int n1 = 0; n1 < cards.length; n1++) {
			find: {
			for (int n2 = 0; n2 < cards.length; n2++) {
				if (cards[n1].equals(nextCards[n2])) {
					kept[i++] = cards[n1];
					break find;
				}
			}
			discarded[j++] = cards[n1];
		}
		}
	}
	
	/**
	 * Get cards player had on this street
	 */
	public static CardsState getCards(final Hand hand, final Seat seat, final int streetIndex, String[] blockers) {
		if (seat.downCards == null && seat.upCards == null) {
			// nothing to base return value on
			// note that only stud has up cards and the hole cards could be null
			return null;
		}
		
		final CardsState cs;
		
		switch (hand.game.type) {
			case FSTUD: {
				String[] cards = HandUtil.getCards(hand, seat);
				// 0 -> 2, 1 -> 3, 2 -> 4, 3 -> 5
				if (streetIndex < 3) {
					cards = Arrays.copyOf(cards, streetIndex + 2);
				}
				// don't sort (though could sort first two)
				return new CardsState(cards, null, false, null);
			}
			
			case STUD:
			case STUDHL:
			case RAZZ: {
				// XXX should maybe emphasise difference between down and up cards
				String[] cards = HandUtil.getCards(hand, seat);
				if (streetIndex < 4) {
					cards = Arrays.copyOf(cards, streetIndex + 3);
				}
				// don't sort (though could sort first two)
				return new CardsState(cards, null, false, null);
			}
			
			case AFTD:
			case BG:
			case FCD:
			case DSTD:
			case DSSD:
				if (GameUtil.isShowdown(hand.game.type, streetIndex)) {
					// on final street just return final hand from seat
					cs = new CardsState(seat.downCards.clone(), null, false, null);
					
				} else {
					// get the draw method for the value type for the game
					final Value v = GameUtil.getPoker(hand.game.type).getValue();
					
					if (hand.myseat == seat) {
						// get current player cards but also see which ones were kept
						String[] cards = hand.myDrawCards(streetIndex);
						if (cards == null) {
							return null;
						}
						System.out.println("my hole cards for street " + streetIndex + " are " + Arrays.toString(cards));
						
						String[] nextCards = hand.myDrawCards(streetIndex + 1);
						if (nextCards == null) {
							nextCards = hand.myseat.downCards;
						}
						System.out.println("my hole cards for street " + (streetIndex+1) + " are " + Arrays.toString(nextCards));
						
						final ArrayList<Draw> drawList = new ArrayList<>();
						final int drawn = seat.drawn(streetIndex);
						v.draw(cards, drawn, blockers, drawList);
						final String[] in = new String[v.cards - drawn];
						final String[] out = new String[drawn];
						kept(cards, nextCards, in, out);
						cs = new CardsState(in, out, false, drawList);
						
					} else {
						// guess opponents hole cards based on final hand
						final ArrayList<Draw> l = new ArrayList<>();
						final int drawn = seat.drawn(streetIndex);
						final String[] h = v.draw(seat.downCards, drawn, blockers, l);
						cs = new CardsState(h, null, true, l);
					}
				}
				break;
				
			case HE:
			case OM:
			case OMHL:
			case OM5:
			case OM51:
			case OM5HL:
			case OM51HL:
				cs = new CardsState(seat.downCards, null, false, null);
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
