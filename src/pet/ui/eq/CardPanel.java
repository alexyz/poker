package pet.ui.eq;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

/**
 * A panel that shoes the given list of card labels in the west of a border layout
 */
class CardPanel extends JPanel {

	private final CardLabel[] cardLabels;
	private final int mincards;
	
	/**
	 * create a card panel with max cards cards
	 */
	public CardPanel(String name, int mincards, int maxcards) {
		super(new GridBagLayout());
		this.mincards = mincards;
		setBorder(BorderFactory.createTitledBorder(name));
		cardLabels = new CardLabel[maxcards];
		
		JPanel p = new JPanel(new GridLayout(1, maxcards, 5, 5));
		for (int n = 0; n < cardLabels.length; n++) {
			CardLabel cl = new CardLabel();
			cardLabels[n] = cl;
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
	public List<CardLabel> getCardLabels() {
		return Collections.unmodifiableList(Arrays.asList(cardLabels));
	}
	
	/**
	 * set all cards to blank
	 */
	public void clearCards() {
		for (CardLabel cl : cardLabels) {
			cl.setCard(null);
		}
	}
	
	/**
	 * set the given cards to blank (indexed from 0)
	 */
	public void clearCards(int from, int to) {
		for (int n = from; n < to; n++) {
			cardLabels[n].setCard(null);
		}
	}
	
	public void setCard(String c, int n) {
		cardLabels[n].setCard(c);
	}
	
	/**
	 * clears all cards and calls setCard for each card label
	 */
	public void setCards(String[] cards) {
		clearCards();
		for (int n = 0; n < Math.min(cardLabels.length, cards.length); n++) {
			cardLabels[n].setCard(cards[n]);
		}
	}
	
	public int getCardCount() {
		int c = 0;
		for (CardLabel cl : cardLabels) { 
			if (cl.getCard() != null) {
				c++;
			}
		}
		return c;
	}
	
	public int getMinCards() {
		return mincards;
	}
	
	/**
	 * Get the cards displayed
	 */
	public String[] getCards() {
		return CardLabel.getCards(Arrays.asList(cardLabels));
	}
	
	public void setCardsHidden(boolean hide) {
		for (CardLabel cl : cardLabels) {
			cl.setCardHidden(hide);
		}
	}
}
