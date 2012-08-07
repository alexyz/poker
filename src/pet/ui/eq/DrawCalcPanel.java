package pet.ui.eq;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;

import pet.eq.*;

/**
 * calc panel for draw hands
 */
public class DrawCalcPanel extends CalcPanel {
	
	public static final String DSLOW = "2-7 Low";
	public static final String HIGH = "High";
	
	private final JComboBox pokerBox = new JComboBox();
	private final HandCardPanel[] handPanels = new HandCardPanel[6];
	
	public DrawCalcPanel() {
		for (int n = 0; n < handPanels.length; n++) {
			handPanels[n] = new HandCardPanel("Draw hand " + (n + 1), 1, 5);
		}
		
		setCardPanels(handPanels);
		
		initCardLabels();
		
		PokerItem[] items = new PokerItem[] {
				new PokerItem(HIGH, new DrawPoker(true)),
				new PokerItem(DSLOW, new DrawPoker(false)),
		};
		
		pokerBox.setModel(new DefaultComboBoxModel(items));
		addCalcOpt(pokerBox);
		
		selectCard(0);
	}
	
	/**
	 * display the given hand
	 */
	public void displayHand(String[][] holeCards, String type) {
		displayHand(null, holeCards, null);
		
		for (int n = 0; n < pokerBox.getItemCount(); n++) {
			PokerItem p = (PokerItem) pokerBox.getItemAt(n);
			if (p.name.equals(type)) {
				pokerBox.setSelectedIndex(n);
				break;
			}
		}
	}
	
	@Override
	protected void hideOpp(boolean hide) {
		for (int n = 1; n < handPanels.length; n++) {
			handPanels[n].setCardsHidden(hide);
		}
	}
	
	@Override
	protected void calc() {
		// XXX could be 0
		String[][] hands = HandCardPanel.getCards(handPanels);
		if (hands != null) {
			PokerItem item = (PokerItem) pokerBox.getSelectedItem();
			String[] blockers = getBlockers();
			MEquity[] meqs = item.poker.equity(null, hands, blockers);
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
	
}

/** represents a poker valuation type in the combo box */
class PokerItem {
	public final String name;
	public final Poker poker;
	public PokerItem(String name, Poker poker) {
		this.name = name;
		this.poker = poker;
	}
	@Override
	public String toString() {
		return name;
	}
}
