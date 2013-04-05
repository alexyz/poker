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
	
	private final JComboBox<StudStreetItem> randStreet = new JComboBox<>();
	
	public StudCalcPanel() {
		HandCardPanel[] handPanels = new HandCardPanel[8];
		for (int n = 0; n < handPanels.length; n++) {
			handPanels[n] = new HandCardPanel("Stud hand " + (n + 1), 1, 7, true);
		}
		setHandCardPanels(handPanels);
		
		CardPanel boardPanel = new CardPanel("Community Card", 0, 1);
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
		setPokerItems(items);
		
		initCardLabels();
		
		selectCard(1);
	}
	
	@Override
	protected void hideOpp(boolean hidden) {
		for (int n = 1; n < handCardPanels.length; n++) {
			List<CardButton> cardLabels = handCardPanels[n].getCardButtons();
			cardLabels.get(0).setCardHidden(hidden);
			cardLabels.get(1).setCardHidden(hidden);
			cardLabels.get(6).setCardHidden(hidden);
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
			boardCardPanel.setCard(deck[51], 0);
		}
		
		for (int n = 0; n < numPlayers; n++) {
			handCardPanels[n].setCards(Arrays.asList(Arrays.copyOfRange(deck, n * numCards, (n + 1) * numCards)));
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
