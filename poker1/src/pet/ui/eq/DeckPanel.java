package pet.ui.eq;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import pet.eq.*;

/**
 * panel to show a whole deck of cards
 */
class DeckPanel extends JPanel {
	private final List<CardLabel> labels = new ArrayList<CardLabel>();
	public DeckPanel() {
		super(new GridLayout(4, 13));
		setBorder(BorderFactory.createTitledBorder("Deck"));
		PropertyChangeListener l = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
			}
		};
		String[] deck = Poker.FULL_DECK.clone();
		Collections.reverse(Arrays.asList(deck));
		for (char s : Poker.suits) {
			for (String c : deck) {
				if (Poker.suit(c) == s) {
					CardLabel cl = new CardLabel(c);
					cl.addPropertyChangeListener(CardLabel.CARD_SEL_PROP_CHANGE, l);
					cl.addPropertyChangeListener(CardLabel.CARD_DESEL_PROP_CHANGE, l);
					add(cl);
					labels.add(cl);
				}
			}
		}
	}

	public void setCardsHidden(boolean hide) {
		for (CardLabel cl : labels) {
			cl.setCardHidden(hide);
		}
	}

	public void deselectCard(String card) {
		for (CardLabel cl : labels) {
			if (cl.getName().equals(card)) {
				cl.setCardSelected(false);
			}
		}
	}
	
	public void selectCards(List<CardLabel> cls) {
		for (CardLabel dcl : labels) {
			for (CardLabel hcl : cls) {
				if (hcl.getName() != null && dcl.getName().equals(hcl.getName())) {
					dcl.setCardSelected(true);
				}
			}
		}
	}
	
	public String[] getCards(boolean sel) {
		List<String> cards = new ArrayList<String>();
		for (CardLabel cl : labels) {
			if (cl.isCardSelected() == sel) {
				cards.add(cl.getCard());
			}
		}
		return cards.toArray(new String[cards.size()]);
	}

	public void selectCards(String[] cards) {
		deselectCards();
		for (CardLabel cl : labels) {
			for (String c : cards) {
				if (cl.getName().equals(c)) {
					cl.setCardSelected(true);
				}
			}
		}
	}

	public void deselectCards() {
		for (CardLabel cl : labels) {
			cl.setCardSelected(false);
		}
	}
}