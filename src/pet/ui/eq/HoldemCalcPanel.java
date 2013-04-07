package pet.ui.eq;

import java.util.*;

import javax.swing.*;

import pet.eq.impl.HEPoker;

/**
 * displays equity calculation panel specific to holdem/omaha
 */
public class HoldemCalcPanel extends CalcPanel {
	
	private final JCheckBox randHandsBox = new JCheckBox("Hole Cards");
	private final JCheckBox randFlopBox = new JCheckBox("Flop");
	private final JCheckBox randTurnBox = new JCheckBox("Turn");
	private final JCheckBox randRiverBox = new JCheckBox("River");
	private final int max;
	
	public HoldemCalcPanel(String name, int min, int max) {
		this.max = max;
		
		// create board and hands and collect card labels
		CardPanel boardPanel = new CardPanel("Community Cards", 0, 5);
		setBoard(boardPanel);
		
		PokerItem[] items = new PokerItem[] {
				new PokerItem(PokerItem.HIGH, new HEPoker(max > 2, false)),
				new PokerItem(PokerItem.HILO, new HEPoker(max > 2, true))
		};
		setPokerItems(items);
		
		// fl holdem has 10 hands, 5 card has max of 9
		HandCardPanel[] handPanels = new HandCardPanel[Math.min(10, 47 / max)];
		for (int n = 0; n < handPanels.length; n++) {
			handPanels[n] = new HandCardPanel(name + " hand " + (n+1), min, max, false);
		}
		// add to layout
		setHandCardPanels(handPanels);
		
		initCardLabels();
		
		// select first hole card
		selectCard(5);
		
		randHandsBox.setSelected(true);
		randFlopBox.setSelected(true);
		
		addRandOpt(randHandsBox);
		addRandOpt(randFlopBox);
		addRandOpt(randTurnBox);
		addRandOpt(randRiverBox);
	}
	
	@Override
	public void random(int numhands) {
		System.out.println("holdem panel random hand");
		
		// clear
		for (HandCardPanel hp : handCardPanels) {
			if (randHandsBox.isSelected()) {
				hp.clearCards();
			}
			hp.setEquity(null);
		}
		if (randHandsBox.isSelected() && max > 4) {
			boardCardPanel.setCard(null, 0);
		}
		if (randFlopBox.isSelected()) {
			if (max <= 4) {
				boardCardPanel.setCard(null, 0);
			}
			boardCardPanel.setCard(null, 1);
			boardCardPanel.setCard(null, 2);
		}
		if (randFlopBox.isSelected() || randTurnBox.isSelected()) {
			boardCardPanel.setCard(null, 3);
		}
		if (randFlopBox.isSelected() || randTurnBox.isSelected() || randRiverBox.isSelected()) {
			boardCardPanel.setCard(null, 4);
		}
		
		// update deck, get remaining cards
		updateDeck();
		List<String> deck = getDeck();
		Collections.shuffle(deck);
		
		int i = 0;
		if (randHandsBox.isSelected()) {
			for (int n = 0; n < numhands; n++) {
				handCardPanels[n].setCards(deck.subList(i, i + max));
				i += max;
			}
			if (max > 4) {
				boardCardPanel.setCard(deck.get(i++), 0);
			}
		}
		
		if (randFlopBox.isSelected()) {
			if (max <= 4) {
				boardCardPanel.setCard(deck.get(i++), 0);
			}
			boardCardPanel.setCard(deck.get(i++), 1);
			boardCardPanel.setCard(deck.get(i++), 2);
		}
		if (randTurnBox.isSelected()) {
			boardCardPanel.setCard(deck.get(i++), 3);
		}
		if (randRiverBox.isSelected()) {
			boardCardPanel.setCard(deck.get(i++), 4);
		}
		
		updateDeck();
		
		selectCard(5);
	}

}
