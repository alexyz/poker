package pet.ui.eq;

import java.awt.GridLayout;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;

import pet.eq.*;

/**
 * panel to show a whole deck of cards
 */
class DeckPanel extends JPanel {
	
	public static final String CARD_SEL_PROP_CHANGE = "cardsel", CARD_DESEL_PROP_CHANGE = "carddesel";
	private final List<CardButton> cardButtons = new ArrayList<>();
	
	public DeckPanel() {
		super(new GridLayout(4, 13));
		setBorder(BorderFactory.createTitledBorder("Deck"));
		
		List<String> deck = new ArrayList<>(Poker.deck);
		Collections.reverse(deck);
		for (char s : Poker.suits) {
			for (String c : deck) {
				if (Poker.suit(c) == s) {
					final CardButton b = new CardButton();
					b.setName("Deck-" + c);
					b.setCard(c);
					b.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (b.isSelected()) {
								System.out.println("deck panel card selected: " + b.getName());
								firePropertyChange(CARD_SEL_PROP_CHANGE, "x", b.getCard());
							} else {
								System.out.println("deck panel card deselected: " + b.getName());
								firePropertyChange(CARD_DESEL_PROP_CHANGE, "x", b.getCard());
							}
						}
					});
					add(b);
					cardButtons.add(b);
				}
			}
		}
	}

	/** call setCardHidden on all the card buttons */
	public void setCardsHidden(boolean hide) {
		for (CardButton b : cardButtons) {
			b.setCardHidden(hide);
		}
	}
	
	/** select the given card, return true if the card was selected or is already selected */
	public boolean setCardSelected(String card, boolean selected) {
		for (CardButton b : cardButtons) {
			if (b.getCard().equals(card)) {
				b.setSelected(selected);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * get the cards by selection status.
	 * always returns a new list
	 */
	public List<String> getCards(boolean selected) {
		List<String> cards = new ArrayList<>();
		for (CardButton b : cardButtons) {
			if (b.isSelected() == selected) {
				cards.add(b.getCard());
			}
		}
		return cards;
	}

	/**
	 * deselect all cards
	 */
	public void deselectCards() {
		for (CardButton b : cardButtons) {
			if (b.isSelected()) {
				b.setSelected(false);
			}
		}
	}
	
}
