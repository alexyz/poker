package pet.ui.eq;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

/**
 * displays a deck and the game specific calc panel
 * [deck] gridy=0
 *   [board] setboard() gridy=1
 *   [hands scroll] sethands() gridy=2
 * [blockers] gridy=3
 * [[rand opts] hide rand] addrandopt() gridy=4
 * [num blockers clear [calc opts] calc] addcalcopt() gridy=5
 */
abstract class CalcPanel extends JPanel {
	
	private final DeckPanel deckPanel = new DeckPanel();
	private final JPanel randPanel = new JPanel();
	private final JPanel randOptsPanel = new JPanel();
	private final JPanel calcPanel = new JPanel();
	private final JPanel calcOptsPanel = new JPanel();
	private final CardPanel blockersCardPanel = new CardPanel("Blockers", 0, 10);
	private final JSpinner numOppSpinner = new JSpinner();
	private final JCheckBox hideBox = new JCheckBox("Hide Opp.");
	private final JButton clearButton = new JButton("Clear");
	private final JButton randButton = new JButton("Random");
	private final JButton calcButton = new JButton("Calculate");

	/**
	 * the list of card labels used by the board and player hole cards. subclass
	 * should add its card labels to this
	 */
	protected final List<CardLabel> cardLabels = new ArrayList<CardLabel>();
	/** currently selected card */
	private int selcard;

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
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		//c.weightx = 1;
		//c.weighty = 1;
		add(deckPanel, c);
		
		c.gridy = 3;
		add(blockersCardPanel, c);
		
		hideBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				hideOpp(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		
		randButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int num = ((SpinnerNumberModel)numOppSpinner.getModel()).getNumber().intValue();
				random(num);
			}
		});
		
		randPanel.add(randOptsPanel);
		randPanel.add(hideBox);
		randPanel.add(randButton);
		c.gridy = 4;
		add(randPanel, c);
		
		// TODO num opps spinner listener
		
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clear();
			}
		});

		calcButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				calc();
			}
		});
		
		calcPanel.add(numOppSpinner);
		calcPanel.add(calcOptsPanel);
		calcPanel.add(clearButton);
		calcPanel.add(calcButton);
		c.gridy = 5;
		add(calcPanel, c);
	}
	
	protected void setBoard(CardPanel board) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 1;
		add(board, c);
	}
	
	/**
	 * Set the card panels created by the subclass (not the actual hands)
	 */
	protected void setCardPanels(CardPanel[] cardPanels) {
		numOppSpinner.setModel(new SpinnerNumberModel(2, 1, cardPanels.length, 1));
		
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		//c.weighty = 1;
		for (CardPanel cp : cardPanels) {
			p.add(cp, c);
			c.gridy++;
		}
		
		JScrollPane sp = new JScrollPane(p, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		add(sp, c);
	}
	
	protected void addRandOpt(JComponent c) {
		randOptsPanel.add(c);
	}
	
	protected void addCalcOpt(JComponent c) {
		calcOptsPanel.add(c);
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

	/** select the given board/hole card number */
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
	 * get unselected deck cards.
	 * always returns a new list
	 */
	protected List<String> getDeck() {
		return deckPanel.getCards();
	}

	protected abstract void random(int num);

	protected abstract void calc();

	/** hide the cards in the deck. subclass should also hide opponents hole card */
	protected void hideOpp(boolean hide) {
		deckPanel.setCardsHidden(hide);
	}

}
