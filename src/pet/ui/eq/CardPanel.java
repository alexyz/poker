package pet.ui.eq;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

/**
 * A panel that shoes the given list of card labels in the west of a border layout
 */
class CardPanel extends JPanel {

	private final CardButton[] cardButtons;
	private final int mincards;
	
	/**
	 * create a card panel with max cards cards
	 */
	public CardPanel(String name, int mincards, int maxcards) {
		super(new GridBagLayout());
		this.mincards = mincards;
		setBorder(BorderFactory.createTitledBorder(name));
		cardButtons = new CardButton[maxcards];
		
		JPanel p = new JPanel(new GridLayout(1, maxcards, 5, 5));
		for (int n = 0; n < cardButtons.length; n++) {
			CardButton cl = new CardButton();
			cardButtons[n] = cl;
			p.add(cl);
		}
		
		GridBagConstraints g = new GridBagConstraints();
		g.gridx = 0;
		g.gridy = 0;
		add(p, g);
	}
	
	protected void addDetails(JComponent c, boolean below) {
		GridBagConstraints g = new GridBagConstraints();
		g.gridx = below ? 0 : 1;
		g.gridy = below ? 1 : 0;
		g.weightx = 1;
		g.fill = GridBagConstraints.BOTH;
		add(c, g);
	}
	
	/**
	 * Get the card labels.
	 */
	public List<CardButton> getCardButtons() {
		return Collections.unmodifiableList(Arrays.asList(cardButtons));
	}
	
	/**
	 * set all cards to blank
	 */
	public void clearCards() {
		for (CardButton b : cardButtons) {
			b.setCard(null);
		}
	}
	
	public void setCard(String c, int n) {
		cardButtons[n].setCard(c);
	}
	
	/**
	 * calls set card for each card label with the given cards or null
	 */
	public void setCards(List<String> cards) {
		for (int n = 0; n < cardButtons.length; n++) {
			cardButtons[n].setCard(n < cards.size() ? cards.get(n) : null);
		}
	}
	
	public int getMinCards() {
		return mincards;
	}
	
	/**
	 * Get the cards displayed
	 */
	public List<String> getCards() {
		List<String> cards = new ArrayList<String>();
		for (CardButton b : cardButtons) {
			if (b.getCard() != null) {
				cards.add(b.getCard());
			}
		}
		return cards;
	}
	
	public void setCardsHidden(boolean hide) {
		for (CardButton cl : cardButtons) {
			cl.setCardHidden(hide);
		}
	}
	
}
