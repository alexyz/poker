package pet.ui.eq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

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
			handPanels[n].collectCardLabels(cardLabels);
		}
		
		initCardLabels();
		
		setCardPanels(handPanels);
		
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
	public void displayHand(String[][] holes, String type) {
		clear();
		for (int n = 0; n < holes.length; n++) {
			handPanels[n].setCards(Arrays.asList(holes[n]));
		}
		updateDeck();
		// should update value in num opp spinner also
		
		for (int n = 0; n < pokerBox.getItemCount(); n++) {
			PokerItem p = (PokerItem) pokerBox.getItemAt(n);
			if (p.name.equals(type)) {
				pokerBox.setSelectedIndex(n);
				break;
			}
		}
	}
	
	@Override
	protected void calc() {
		// XXX could be 0
		String[][] hands = HandCardPanel.getCards(handPanels);
		if (hands != null) {
			PokerItem item = (PokerItem) pokerBox.getSelectedItem();
			// FIXME no blockers
			MEquity[] meqs = item.poker.equity(null, hands, null);
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
