package pet.ui.eq;

import java.util.*;
import javax.swing.*;

import pet.eq.*;

public class HoldemCalcPanel extends CalcPanel {
	private final BoardPanel boardPanel;
	private final HandCardPanel[] handPanels = new HandCardPanel[6];
	// TODO in a random panel
	private final JCheckBox randHandsBox = new JCheckBox("Hands");
	private final JCheckBox randFlopBox = new JCheckBox("Flop");
	private final JCheckBox randTurnBox = new JCheckBox("Turn");
	private final JCheckBox randRiverBox = new JCheckBox("River");
	private final boolean omaha;
	private final int numHoleCards;

	public HoldemCalcPanel(boolean omaha) {
		this.omaha = omaha;
		this.numHoleCards = omaha ? 4 : 2;
		
		// create board and hands and collect card labels
		boardPanel = new BoardPanel();
		boardPanel.collectCardLabels(cardLabels);
		for (int n = 0; n < handPanels.length; n++) {
			handPanels[n] = new HoldemHandPanel(n + 1, omaha);
			handPanels[n].collectCardLabels(cardLabels);
		}
		
		// add to layout
		addgb(boardPanel);
		for (HandCardPanel hp : handPanels) {
			addgb(hp);
		}
		
		initCardLabels();
		
		// select first hole card
		selectCard(5);
		
		randHandsBox.setSelected(true);
		randFlopBox.setSelected(true);
		JPanel p = new JPanel();
		p.add(randHandsBox);
		p.add(randFlopBox);
		p.add(randTurnBox);
		p.add(randRiverBox);
		addgb(p);
		addgb(new ButtonPanel(this));
	}
	
	/**
	 * display the given hand
	 */
	public void displayHand(String[] board, String[][] holes) {
		clear();
		boardPanel.setCards(board);
		for (int n = 0; n < holes.length; n++) {
			handPanels[n].setCards(holes[n]);
		}
		updateDeck();
	}

	@Override
	public void hideOpp(boolean hide) {
		super.hideOpp(hide);
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
			hp.clearHandEquity();
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
		String[] deck = getDeck();
		RandomUtil.shuffle(deck);
		
		int i = 0;
		if (randHandsBox.isSelected()) {
			for (int n = 0; n < numhands; n++) {
				handPanels[n].setCards(Arrays.copyOfRange(deck, i, i + numHoleCards));
				i += numHoleCards;
			}
		}
		if (randFlopBox.isSelected()) {
			boardPanel.setCard(deck[i++], 0);
			boardPanel.setCard(deck[i++], 1);
			boardPanel.setCard(deck[i++], 2);
		}
		if (randTurnBox.isSelected()) {
			boardPanel.setCard(deck[i++], 3);
		}
		if (randRiverBox.isSelected()) {
			boardPanel.setCard(deck[i++], 4);
		}
		
		updateDeck();
		selectCard(5);
	}

	@Override
	public void calc() {
		List<String[]> hl = new ArrayList<String[]>();
		for (HandCardPanel hp : handPanels) {
			hp.clearHandEquity();
		}
		
		String[] board = boardPanel.getCards();
		if (board.length == 1 || board.length == 2) {
			System.out.println("incomplete board");
			return;
		}
		
		List<HandCardPanel> pl = new ArrayList<HandCardPanel>();
		for (HandCardPanel hp : handPanels) {
			String[] hand = hp.getCards();
			if (hand.length > 0) {
				if (hand.length < hp.getMinCards()) {
					System.out.println("incomplete hand");
					return;
					
				} else {
					pl.add(hp);
					hl.add(hand);
				}
			}
		}

		if (hl.size() == 0) {
			System.out.println("no hands");
			return;
		}

		String[][] hands = hl.toArray(new String[hl.size()][]);
		HandEq[] eqs = new HEPoker(omaha).equity(board, hands, null);
		for (int n = 0; n < eqs.length; n++) {
			HandEq e = eqs[n];
			pl.get(n).setHandEquity(e);
			System.out.println("outs: " + e.outs);
		}

	}

	/**
	 * clear the deck, the board and the hand panels and select first hole card
	 */
	@Override
	public void clear() {
		super.clear();
		boardPanel.clearCards();
		for (HandCardPanel hp : handPanels) {
			hp.clearCards();
		}
		selectCard(5);
	}

}
