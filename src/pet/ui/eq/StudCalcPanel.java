package pet.ui.eq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.*;

import pet.eq.*;

/**
 * calc panel for stud hands
 */
public class StudCalcPanel extends CalcPanel {
	
	
	private final HandCardPanel[] handPanels = new HandCardPanel[8];
	private final CardPanel boardPanel = new CardPanel("Community Card", 0, 1);
	private final JComboBox<StudStreetItem> randStreet = new JComboBox<>();
	private final JComboBox<PokerItem> pokerCombo = new JComboBox<>();
	
	public StudCalcPanel() {
		for (int n = 0; n < handPanels.length; n++) {
			handPanels[n] = new HandCardPanel("Stud hand " + (n + 1), 1, 7, true);
		}
		
		setHandCardPanels(handPanels);
		setBoard(boardPanel);
		
		randStreet.setModel(new DefaultComboBoxModel<>(new StudStreetItem[] {
				new StudStreetItem("3rd", 3),
				new StudStreetItem("4th", 4),
				new StudStreetItem("5th", 5),
				new StudStreetItem("6th", 6),
				new StudStreetItem("7th", 7)
		}));
		
		addRandOpt(randStreet);

		PokerItem[] items = new PokerItem[] {
				new PokerItem(PokerItem.HIGH, new StudPoker(Value.hiValue, false)),
				new PokerItem(PokerItem.AFLOW, new StudPoker(Value.afLowValue, false)),
				new PokerItem(PokerItem.HILO, new StudPoker(Value.hiValue, true))
		};
		pokerCombo.setModel(new DefaultComboBoxModel<>(items));
		
		addCalcOpt(pokerCombo);
		
		initCardLabels();
		
		selectCard(1);
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
	protected void hideOpp(boolean hidden) {
		for (int n = 1; n < handPanels.length; n++) {
			List<CardButton> cardLabels = handPanels[n].getCardButtons();
			cardLabels.get(0).setCardHidden(hidden);
			cardLabels.get(1).setCardHidden(hidden);
			cardLabels.get(6).setCardHidden(hidden);
		}
	}
	
	@Override
	protected void calc() {
		// TODO validate, e.g. no seventh card if board card
		
		for (HandCardPanel hp : handPanels) {
			hp.setEquity(null);
		}
		
		// get the hands and the panels of those hands
		List<HandCardPanel> cardPanels = new ArrayList<>();
		List<String[]> cards = new ArrayList<>();
		collectCards(cards, cardPanels);
		if (cards.size() == 0) {
			System.out.println("no hands");
			return;
		}
		
		// get other stuff and calc equity
		List<String> blockers = getBlockers();
		List<String> board = boardPanel.getCards();
		PokerItem pokerItem = (PokerItem) pokerCombo.getSelectedItem();
		MEquity[] meqs = pokerItem.poker.equity(board, cards, blockers, 0);
		
		// update ui
		for (int n = 0; n < meqs.length; n++) {
			cardPanels.get(n).setEquity(meqs[n]);
		}
	}
	
	@Override
	protected void random(int numPlayers) {
		int numCards = ((StudStreetItem) randStreet.getSelectedItem()).num;
		
		clear();
		String[] deck = Poker.deck();
		ArrayUtil.shuffle(deck, new Random());
		
		if (numPlayers >= 8 && numCards == 7) {
			numCards = 6;
			boardPanel.setCard(deck[51], 0);
		}
		
		for (int n = 0; n < numPlayers; n++) {
			handPanels[n].setCards(Arrays.asList(Arrays.copyOfRange(deck, n * numCards, (n + 1) * numCards)));
		}
		updateDeck();
	}
	
}

class StudStreetItem {
	public final String name;
	public final int num;
	public StudStreetItem(String name, int num) {
		this.name = name;
		this.num = num;
	}
	@Override
	public String toString() {
		return name;
	}
}
