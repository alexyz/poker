package pet.ui.eq;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * displays a deck and the game specific calc panel
 */
abstract class CalcPanel extends JPanel {
	
	/**
	 * the list of card labels used by the board and player hole cards. subclass
	 * should add its card labels to this
	 */
	protected final List<CardLabel> cardLabels = new ArrayList<CardLabel>();
	private final DeckPanel deckPanel = new DeckPanel();
	/** currently selected card */
	private int selcard;
	private GridBagConstraints c = new GridBagConstraints();

	public CalcPanel() {
		setLayout(new GridBagLayout());
		deckPanel.addPropertyChangeListener(CardLabel.CARD_SEL_PROP_CHANGE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				cardLabels.get(selcard).setCard((String) e.getNewValue());
				selectCard(selcard + 1);
			}
		});
		deckPanel.addPropertyChangeListener(CardLabel.CARD_DESEL_PROP_CHANGE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				for (int n = 0; n < cardLabels.size(); n++) {
					CardLabel cl = cardLabels.get(n);
					String c = cl.getCard();
					if (c != null && c.equals(e.getNewValue())) {
						cl.setCard(null);
						selectCard(n);
						break;
					}
				}
			}
		});
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		addgb(deckPanel);
	}

	protected void addgb(JComponent comp) {
		add(comp, c);
		c.gridy++;
	}

	/** add selection listener to all the card labels */
	protected void initCardLabels() {
		for (final CardLabel cl : cardLabels) {
			cl.addPropertyChangeListener(CardLabel.CARD_SEL_PROP_CHANGE, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					deckPanel.deselectCard(cl.getCard());
					cl.setCard(null);
					cardLabels.get(selcard).setCardSelected(false);
					selcard = cardLabels.indexOf(cl);
				}
			});
		}
	}

	protected void selectCard(int n) {
		System.out.println("select card " + n);
		cardLabels.get(selcard).setCardSelected(false);
		selcard = n % cardLabels.size();
		cardLabels.get(selcard).setCardSelected(true);
	}

	/**
	 * clear the deck
	 */
	protected void clear() {
		deckPanel.deselectCards();
	}
	
	/**
	 * Select the deck cards according to cards displayed in the card labels
	 */
	protected void updateDeck() {
		deckPanel.selectCards(CardLabel.getCards(cardLabels));
	}
	
	/**
	 * get unselected deck cards
	 */
	protected String[] getDeck() {
		return deckPanel.getCards();
	}

	protected abstract void random(int num);

	protected abstract void calc();

	/** hide the cards in the deck. subclass should also hide opponents hole card */
	protected void hideOpp(boolean hide) {
		deckPanel.setCardsHidden(hide);
	}

}
