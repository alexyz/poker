package pet.ui.eq;

import java.util.*;
import javax.swing.*;
import pet.*;

public class HoldemCalcPanel extends CalcPanel {
	private final BoardPanel boardPanel;
	private final HandCardPanel[] handPanels = new HandCardPanel[4];
	private final JCheckBox randHandsBox = new JCheckBox("Hands");
	private final JCheckBox randFlopBox = new JCheckBox("Flop");
	private final JCheckBox randTurnBox = new JCheckBox("Turn");
	private final JCheckBox randRiverBox = new JCheckBox("River");
	private final boolean isTexas;
	private final int numHoleCards;

	public HoldemCalcPanel(boolean tx) {
		this.isTexas = tx;
		this.numHoleCards = tx ? 2 : 4;
		boardPanel = new BoardPanel(cardLabels);
		for (int n = 0; n < handPanels.length; n++) {
			handPanels[n] = new HoldemHandPanel(cardLabels, n + 1, tx);
		}
		initCardLabels();
		selectCard(5);
		addgb(boardPanel);
		for (HandCardPanel hp : handPanels) {
			addgb(hp);
		}
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
		deckPanel.deselectCards();
		deckPanel.selectCards(cardLabels);
		String[] deck = deckPanel.getCards(false);
		Util.shuffle(deck);
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
		deckPanel.selectCards(cardLabels);
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
		HandEq[] eqs = new HEPoker().equity(board, hands, !isTexas);
		for (int n = 0; n < eqs.length; n++) {
			HandEq e = eqs[n];
			pl.get(n).setHandEquity(e);
			System.out.println("outs: " + e.outs);
		}

	}

	@Override
	public void clear() {
		// XXX should be in super?
		super.clear();
		boardPanel.clearCards();
		for (HandCardPanel hp : handPanels) {
			hp.clearCards();
		}
		selectCard(5);
	}

}