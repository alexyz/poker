package pet.ui.eq;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

abstract class CalcPanel extends JPanel {
	protected final List<CardLabel> cardLabels = new ArrayList<CardLabel>();
	protected final DeckPanel deckPanel = new DeckPanel();
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
		cardLabels.get(selcard).setCardSelected(false);
		selcard = n % cardLabels.size();
		cardLabels.get(selcard).setCardSelected(true);
	}

	protected void clear() {
		deckPanel.deselectCards();
	}

	protected abstract void random(int num);

	protected abstract void calc();

	protected void hideOpp(boolean hide) {
		deckPanel.setCardsHidden(hide);
	}


}