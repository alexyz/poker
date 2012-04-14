package pet.ui.eq;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * A panel that shoes the given list of card labels in the west of a border layout
 */
class CardPanel extends JPanel {

	private final CardLabel[] cardLabs;
	private final int mincards;
	
	/**
	 * create a card panel with max cards cards
	 */
	public CardPanel(String name, int mincards, int maxcards) {
		super(new BorderLayout());
		this.mincards = mincards;
		setBorder(BorderFactory.createTitledBorder(name));
		cardLabs = new CardLabel[maxcards];
		JPanel p = new JPanel(new GridLayout(1, maxcards, 5, 5));
		for (int n = 0; n < cardLabs.length; n++) {
			CardLabel cl = new CardLabel();
			cardLabs[n] = cl;
			p.add(cl);
		}
		add(p, BorderLayout.WEST);
	}
	
	/**
	 * Add the card labels to the list
	 */
	public void collectCardLabels(List<CardLabel> l) {
		for (CardLabel cl : cardLabs) {
			l.add(cl);
		}
	}
	
	public void clearCards() {
		for (CardLabel cl : cardLabs) {
			cl.setCard(null);
		}
	}
	
	public void clearCards(int from, int to) {
		for (int n = from; n < to; n++) {
			cardLabs[n].setCard(null);
		}
	}
	
	public void setCard(String c, int n) {
		cardLabs[n].setCard(c);
	}
	
	public void setCards(String[] cs) {
		clearCards();
		for (int n = 0; n < cardLabs.length; n++) {
			cardLabs[n].setCard(n < cs.length ? cs[n] : null);
		}
	}
	
	public int getCardCount() {
		int c = 0;
		for (CardLabel cl : cardLabs) { 
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
		return CardLabel.getCards(Arrays.asList(cardLabs));
	}
	
	public void setCardsHidden(boolean hide) {
		for (CardLabel cl : cardLabs) {
			cl.setCardHidden(hide);
		}
	}
}
