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
 * [[rand opts] hide num rand] addrandopt() gridy=4
 * [[calc opts] clear calc] addcalcopt() gridy=5
 */
public abstract class CalcPanel extends JPanel {
	
	private final DeckPanel deckPanel = new DeckPanel();
	private final JPanel randPanel = new JPanel();
	private final JPanel randOptsPanel = new JPanel();
	private final JPanel calcButtonsPanel = new JPanel();
	private final JPanel calcOptsPanel = new JPanel();
	private final CardPanel blockersCardPanel = new CardPanel("Blockers", 0, 10);
	private final JSpinner randNumOppSpinner = new JSpinner();
	private final JToggleButton hideButton = new JToggleButton("Hide");
	private final JButton clearButton = new JButton("Clear");
	private final JButton randButton = new JButton("Random");
	private final JButton calcButton = new JButton("Calculate");
	private final JScrollPane scroller = new JScrollPane();
	/**
	 * the list of card labels used by the board, player hole cards and blockers
	 */
	private final List<CardLabel> cardLabels = new ArrayList<CardLabel>();
	
	/** currently selected card (cardLabels index) */
	private int selectedCard;
	private CardPanel[] cardPanels;
	private CardPanel boardPanel;

	public CalcPanel() {
		setLayout(new GridBagLayout());
		
		deckPanel.addPropertyChangeListener(CardLabel.CARD_SEL_PROP_CHANGE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				// set the selected card to the deck card and move to next card
				cardLabels.get(selectedCard).setCard((String) e.getNewValue());
				selectCard(selectedCard + 1);
			}
		});
		
		deckPanel.addPropertyChangeListener(CardLabel.CARD_DESEL_PROP_CHANGE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				for (int n = 0; n < cardLabels.size(); n++) {
					CardLabel cl = cardLabels.get(n);
					String c = cl.getCard();
					if (c != null && c.equals(e.getNewValue())) {
						// remove the card
						cl.setCard(null);
						selectCard(n);
						break;
					}
				}
			}
		});
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		add(deckPanel, c);
		
		hideButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean hide = e.getStateChange() == ItemEvent.SELECTED;
				deckPanel.setCardsHidden(hide);
				hideOpp(hide);
			}
		});
		
		randButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int num = ((SpinnerNumberModel)randNumOppSpinner.getModel()).getNumber().intValue();
				random(num);
			}
		});
		
		randPanel.add(randOptsPanel);
		randPanel.add(hideButton);
		randPanel.add(randNumOppSpinner);
		randPanel.add(randButton);
		c.gridy = 4;
		add(randPanel, c);
		
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
		
		calcButtonsPanel.add(calcOptsPanel);
		calcButtonsPanel.add(clearButton);
		calcButtonsPanel.add(calcButton);
		c.gridy = 5;
		add(calcButtonsPanel, c);
	}
	
	/** set the community card panel */
	protected void setBoard(CardPanel boardPanel) {
		this.boardPanel = boardPanel;
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 1;
		add(boardPanel, c);
	}
	
	/**
	 * Set the card panels created by the subclass (not the actual hands)
	 */
	protected void setCardPanels(CardPanel[] cardPanels) {
		this.cardPanels = cardPanels;
		
		randNumOppSpinner.setModel(new SpinnerNumberModel(2, 1, cardPanels.length, 1));
		
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
		p.add(blockersCardPanel, c);
		
		scroller.setViewportView(p);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setPreferredSize(new Dimension(640,320));
		
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		add(scroller, c);
	}
	
	protected void addRandOpt(JComponent c) {
		randOptsPanel.add(c);
	}
	
	protected void addCalcOpt(JComponent c) {
		calcOptsPanel.add(c);
	}

	/**
	 * Add selection listener to all the card labels. Need to call setBoard,
	 * setCardPanels first.
	 */
	protected void initCardLabels() {
		// collect the card labels in selection order
		if (boardPanel != null) {
			for (CardLabel cl : boardPanel.getCardLabels()) {
				cardLabels.add(cl);
			}
		}
		
		for (CardPanel cp : cardPanels) {
			for (CardLabel cl : cp.getCardLabels()) {
				cardLabels.add(cl);
			}
		}
		
		for (CardLabel cl : blockersCardPanel.getCardLabels()) {
			cardLabels.add(cl);
		}
		
		// add the listeners
		for (final CardLabel cl : cardLabels) {
			cl.addPropertyChangeListener(CardLabel.CARD_SEL_PROP_CHANGE, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					deckPanel.deselectCard(cl.getCard());
					cl.setCard(null);
					cardLabels.get(selectedCard).setCardSelected(false);
					selectedCard = cardLabels.indexOf(cl);
				}
			});
		}
	}

	/** select the given board/hole card number. need to call initCardLabels first */
	protected void selectCard(int n) {
		System.out.println("calc panel select card " + n);
		cardLabels.get(selectedCard).setCardSelected(false);
		selectedCard = n % cardLabels.size();
		cardLabels.get(selectedCard).setCardSelected(true);
	}
	
	/**
	 * clear deck, board, hands, blockers and select first hand card
	 */
	protected void clear() {
		deckPanel.deselectCards();
		int n = 0;
		if (boardPanel != null) {
			boardPanel.clearCards();
			n += boardPanel.getCardLabels().size();
		}
		for (CardPanel cp : cardPanels) {
			cp.clearCards();
		}
		blockersCardPanel.clearCards();
		selectCard(n);
		scroller.getVerticalScrollBar().getModel().setValue(0);
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
	
	/**
	 * get blockers
	 */
	protected String[] getBlockers() {
		return blockersCardPanel.getCards();
	}
	
	/** set the board, hands and blockers */
	protected void displayHand(String[] board, List<String[]> holeCards) {
		clear();
		if (board != null) {
			boardPanel.setCards(board);
		}
		for (int n = 0; n < holeCards.size(); n++) {
			cardPanels[n].setCards(holeCards.get(n));
		}
		randNumOppSpinner.setValue(holeCards.size());
		updateDeck();
	}
	
	//
	// methods for subclass
	//
	
	/** display the given board, cards and hand value type */
	public abstract void displayHand(String[] board, List<String[]> cards, String type);
	
	/** random button pressed */
	protected abstract void random(int num);

	/** calc button pressed */
	protected abstract void calc();

	/** hide the cards in the deck. subclass should also hide opponents hole card */
	protected abstract void hideOpp(boolean hide);

}
