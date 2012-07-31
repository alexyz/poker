package pet.ui.eq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import pet.eq.*;

/**
 * calc panel for draw hands
 */
public class DrawCalcPanel extends CalcPanel {
	
	private final HandCardPanel[] handPanels = new HandCardPanel[6];
	
	public DrawCalcPanel() {
		for (int n = 0; n < handPanels.length; n++) {
			handPanels[n] = new HandCardPanel("Draw hand " + (n + 1), 1, 5);
			handPanels[n].collectCardLabels(cardLabels);
		}
		
		initCardLabels();
		
		setCardPanels(handPanels);
	}
	
	/**
	 * display the given hand
	 */
	public void displayHand(String[][] holes) {
		clear();
		for (int n = 0; n < holes.length; n++) {
			handPanels[n].setCards(Arrays.asList(holes[n]));
		}
		updateDeck();
		// should update value in num opp spinner also
	}
	
	@Override
	protected void calc() {
		// XXX could be 0
		String[][] hands = HandCardPanel.getCards(handPanels);
		if (hands != null) {
			MEquity[] meqs = DrawPoker.equityImpl(hands, null);
			for (int n = 0; n < meqs.length; n++) {
				handPanels[n].setHandEquity(meqs[n]);
			}
		} else {
			System.out.println("no hands");
		}
	}
	
	@Override
	protected void random(int num) {
		ArrayList<String> deck = new ArrayList<String>(Poker.deck);
		Collections.shuffle(deck);
		for (int n = 0; n < num; n++) {
			handPanels[n].setCards(deck.subList(n * 5, n * 5 + 5));
		}
		updateDeck();
	}
	
	/**
	 * clear the deck and the hand panels and select first hole card
	 */
	@Override
	public void clear() {
		super.clear();
		for (HandCardPanel hp : handPanels) {
			hp.clearCards();
		}
		selectCard(0);
	}
	
}
