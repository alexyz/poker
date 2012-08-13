package pet.ui.eq;

import java.util.*;
import javax.swing.*;

import pet.eq.*;

/**
 * displays equity calculation panel specific to holdem/omaha
 */
public class HoldemCalcPanel extends CalcPanel {
	
	private final CardPanel boardPanel;
	private final HandCardPanel[] handPanels = new HandCardPanel[10];
	private final JCheckBox randHandsBox = new JCheckBox("Hole Cards");
	private final JCheckBox randFlopBox = new JCheckBox("Flop");
	private final JCheckBox randTurnBox = new JCheckBox("Turn");
	private final JCheckBox randRiverBox = new JCheckBox("River");
	private final JComboBox pokerCombo = new JComboBox();
	private final int numHoleCards;

	public HoldemCalcPanel(boolean omaha) {
		this.numHoleCards = omaha ? 4 : 2;
		
		// create board and hands and collect card labels
		boardPanel = new CardPanel("Community Cards", 0, 5);
		
		pokerCombo.setModel(new DefaultComboBoxModel(new PokerItem[] {
				new PokerItem(PokerItem.HIGH, new HEPoker(omaha, false)),
				new PokerItem(PokerItem.HILO, new HEPoker(omaha, true))
		}));
		
		String name = omaha ? "Omaha" : "Hold'em";
		int min = omaha ? 2 : 1;
		int max = omaha ? 4 : 2;
		for (int n = 0; n < handPanels.length; n++) {
			handPanels[n] = new HandCardPanel(name + " hand " + (n+1), min, max, false);
		}
		
		// add to layout
		setBoard(boardPanel);
		setCardPanels(handPanels);
		
		initCardLabels();
		
		// select first hole card
		selectCard(5);

		randHandsBox.setSelected(true);
		randFlopBox.setSelected(true);
		
		addRandOpt(randHandsBox);
		addRandOpt(randFlopBox);
		addRandOpt(randTurnBox);
		addRandOpt(randRiverBox);
		if (omaha) {
			addCalcOpt(pokerCombo);
		}
	}
	
	/**
	 * display the given hand
	 */
	@Override
	public void displayHand(String[] board, List<String[]> holeCards, String type) {
		displayHand(board, holeCards);
		PokerItem.select(pokerCombo, type);
	}

	@Override
	public void hideOpp(boolean hide) {
		for (int n = 1; n < handPanels.length; n++) {
			handPanels[n].setCardsHidden(hide);
		}
	}

	@Override
	public void random(int numhands) {
		for (HandCardPanel hp : handPanels) {
			if (randHandsBox.isSelected()) {
				hp.clearCards();
			}
			hp.setEquity(null);
		}
		if (randFlopBox.isSelected()) {
			boardPanel.clearCards(0, 3);
		}
		if (randFlopBox.isSelected() || randTurnBox.isSelected()) {
			boardPanel.clearCards(3, 4);
		}
		if (randFlopBox.isSelected() || randTurnBox.isSelected() || randRiverBox.isSelected()) {
			boardPanel.clearCards(4, 5);
		}
		
		// update deck, get remaining cards
		updateDeck();
		List<String> deck = getDeck();
		Collections.shuffle(deck);
		
		int i = 0;
		if (randHandsBox.isSelected()) {
			for (int n = 0; n < numhands; n++) {
				// XXX hack
				handPanels[n].setCards(deck.subList(i, i + numHoleCards).toArray(new String[0]));
				i += numHoleCards;
			}
		}
		if (randFlopBox.isSelected()) {
			boardPanel.setCard(deck.get(i++), 0);
			boardPanel.setCard(deck.get(i++), 1);
			boardPanel.setCard(deck.get(i++), 2);
		}
		if (randTurnBox.isSelected()) {
			boardPanel.setCard(deck.get(i++), 3);
		}
		if (randRiverBox.isSelected()) {
			boardPanel.setCard(deck.get(i++), 4);
		}
		
		updateDeck();
		selectCard(5);
	}

	@Override
	public void calc() {
		for (HandCardPanel hp : handPanels) {
			hp.setEquity(null);
		}
		
		String[] board = boardPanel.getCards();
		if (board.length == 1 || board.length == 2) {
			System.out.println("incomplete board");
			return;
		}
		
		if (board.length == 0) {
			board = null;
		}
		
		final List<String[]> holeCards = new ArrayList<String[]>();
		final List<HandCardPanel> holeCardsHandPanels = new ArrayList<HandCardPanel>();
		
		for (HandCardPanel hp : handPanels) {
			String[] hand = hp.getCards();
			if (hand.length > 0) {
				if (hand.length < hp.getMinCards()) {
					System.out.println("incomplete hand");
					return;
					
				} else {
					holeCardsHandPanels.add(hp);
					holeCards.add(hand);
				}
			}
		}

		if (holeCards.size() == 0) {
			System.out.println("no hands");
			return;
		}
		
		final String[] blockers = getBlockers();
		final String[][] holeCardsArr = holeCards.toArray(new String[holeCards.size()][]);
		final PokerItem pokerItem = (PokerItem) pokerCombo.getSelectedItem();
		final MEquity[] eqs = pokerItem.poker.equity(board, holeCardsArr, blockers);
		
		for (int n = 0; n < eqs.length; n++) {
			holeCardsHandPanels.get(n).setEquity(eqs[n]);
		}

	}

}
