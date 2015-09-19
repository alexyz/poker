package pet.ui.eq;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import pet.eq.*;

/**
 * displays a deck and the game specific calc panel
 */
public abstract class CalcPanel extends JPanel {
	
	private final class CardDeselectedPCL implements PropertyChangeListener {
		
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			System.out.println("calc panel deck card deselected prop change");
			for (int n = 0; n < cardButtons.size(); n++) {
				CardButton cl = cardButtons.get(n);
				String c = cl.getCard();
				if (c != null && c.equals(e.getNewValue())) {
					// remove the card
					cl.setCard(null);
					selectCard(n);
					break;
				}
			}
		}
	}

	private final class CardSelectedPCL implements PropertyChangeListener {
		
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			System.out.println("calc panel deck card selected prop change");
			find: {
				String newCard = (String) e.getNewValue();
				for (int n = 0; n < cardButtons.size(); n++) {
					CardButton b = cardButtons.get(n);
					if (b.isSelected()) {
						// set the selected card to the deck card and move to next card
						String oldCard = b.getCard();
						if (oldCard != null && !oldCard.equals(newCard)) {
							deckPanel.setCardSelected(oldCard, false);
						}
						b.setCard(newCard);
						b.setSelected(false);
						
						// select next button
						CardButton nb = cardButtons.get((n + 1) % cardButtons.size());
						nb.setSelected(true);
						// req focus? will take it from deck
						break find;
					}
				}
				// reject if no card selected
				deckPanel.setCardSelected(newCard, false);
			}
		}
	}

	/** card button key listener */
	private final class CardKL extends KeyAdapter {
		
		char c1 = 0;
		
		@Override
		public void keyTyped(KeyEvent e) {
			CardButton b = (CardButton) e.getSource();
			char c2 = e.getKeyChar();
			System.out.println("calc panel typed " + Integer.toHexString(e.getKeyChar()));
			if (c2 == '\n') {
				System.out.println("-- return --");
				for (CardButton ob : cardButtons) {
					ob.setSelected(false);
				}
				// shift doesn't work
				CardButton nb = cardButtons.get((cardButtons.indexOf(b) + 1) % cardButtons.size());
				nb.setSelected(true);
				nb.requestFocusInWindow();
				c1 = 0;
				
				// TODO backspace
				
			} else if (c1 != 0) {
				String c = new String(new char[] { 
						Character.toUpperCase(c1), 
						Character.toLowerCase(c2) 
				});
				
				// note: may already be selected in deck
				if (deckPanel.setCardSelected(c, true)) {
					// unset card from any other buttons
					for (CardButton ob : cardButtons) {
						ob.setSelected(false);
						if (ob != b && ob.getCard() != null && ob.getCard().equals(c)) {
							ob.setCard(null);
						}
					}
					
					// set the card on this button
					b.setCard(c);
					
					// focus next button
					CardButton nb = cardButtons.get((cardButtons.indexOf(b) + 1) % cardButtons.size());
					nb.setSelected(true);
					nb.requestFocusInWindow();
				}
				c1 = 0;
				
			} else {
				c1 = c2;
			}
		}
	}

	/** card button action listener */
	private final class CardAL implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			CardButton b = (CardButton) e.getSource();
			if (b.isSelected()) {
				System.out.println("calc panel card selected: " + b.getName() + " value " + b.getCard());
				if (b.getCard() != null) {
					// clear card
					deckPanel.setCardSelected(b.getCard(), false);
					b.setCard(null);
				}
				// deselect other cards
				for (CardButton ob : cardButtons) {
					if (ob != b) {
						ob.setSelected(false);
					}
				}
				
			} else {
				System.out.println("calc panel card deselected: " + b.getName() + " value " + b.getCard());
				// do nothing
			}
		}
	}
	
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
	 * the list of all card buttons used by the board, player hole cards and
	 * blockers (in that order)
	 */
	private final List<CardButton> cardButtons = new ArrayList<>();
	private final JComboBox<PokerItem> pokerCombo = new JComboBox<>();
	private final JSpinner drawsSpinner;
	
	// protected to allow access by subclasses
	protected HandCardPanel[] handCardPanels;
	protected CardPanel boardCardPanel;
	
	public CalcPanel() {
		this(false);
	}
	
	/**
	 * create panel with optional draw spinner
	 */
	public CalcPanel(boolean draw) {
		setLayout(new GridBagLayout());
		
		// hack to get the selected card to be focused
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				for (int n = 0; n < cardButtons.size(); n++) {
					CardButton b = cardButtons.get(n);
					if (b.isSelected()) {
						b.requestFocusInWindow();
					}
				}
			}
		});
		
		deckPanel.addPropertyChangeListener(DeckPanel.CARD_SEL_PROP_CHANGE, new CardSelectedPCL());
		
		deckPanel.addPropertyChangeListener(DeckPanel.CARD_DESEL_PROP_CHANGE, new CardDeselectedPCL());
		
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
		randPanel.add(new JLabel("Players"));
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
		
		// used to be in subclass
		
		calcOptsPanel.add(new JLabel("Value"));
		calcOptsPanel.add(pokerCombo);
		
		if (draw) {
			drawsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 3, 1));
			calcOptsPanel.add(new JLabel("Draws"));
			calcOptsPanel.add(drawsSpinner);
			
		} else {
			drawsSpinner = null;
		}
	}
	
	/** set the items to display in the valuation type combo box */
	protected void setPokerItems(PokerItem[] items) {
		pokerCombo.setModel(new DefaultComboBoxModel<>(items));
	}
	
	/** set the community card panel */
	protected void setBoard(CardPanel boardPanel) {
		this.boardCardPanel = boardPanel;
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 1;
		add(boardPanel, c);
	}
	
	/**
	 * Set the card panels created by the subclass (not the actual hands)
	 */
	protected void setHandCardPanels(HandCardPanel[] cardPanels) {
		this.handCardPanels = cardPanels;
		
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
	
	/**
	 * Add selection listener to all the card buttons. Need to call setBoard,
	 * setCardPanels first.
	 */
	protected void initCardButtons() {
		// collect the card labels in selection order
		if (boardCardPanel != null) {
			for (CardButton b : boardCardPanel.getCardButtons()) {
				cardButtons.add(b);
			}
		}
		
		for (CardPanel cp : handCardPanels) {
			for (CardButton b : cp.getCardButtons()) {
				cardButtons.add(b);
			}
		}
		
		for (CardButton b : blockersCardPanel.getCardButtons()) {
			cardButtons.add(b);
		}
		
		// add the listeners
		for (int n = 0; n < cardButtons.size(); n++) {
			final CardButton b = cardButtons.get(n);
			b.setName("Hand-" + n);
			b.addActionListener(new CardAL());
			b.addKeyListener(new CardKL());
		}
	}
	
	/** select the given board/hole card number. need to call initCardLabels first */
	protected void selectCard(int n) {
		System.out.println("calc panel select card " + n);
		CardButton b = cardButtons.get(n);
		b.setSelected(true);
		// deselect other cards
		for (CardButton ob : cardButtons) {
			if (ob != b) {
				ob.setSelected(false);
			}
		}
	}
	
	/**
	 * clear deck, board, hands, blockers and select first hand card
	 */
	protected void clear() {
		deckPanel.deselectCards();
		int n = 0;
		if (boardCardPanel != null) {
			boardCardPanel.clearCards();
			n += boardCardPanel.getCardButtons().size();
		}
		for (CardPanel cp : handCardPanels) {
			cp.clearCards();
		}
		blockersCardPanel.clearCards();
		selectCard(n);
		cardButtons.get(n).requestFocusInWindow();
		scroller.getVerticalScrollBar().getModel().setValue(0);
	}
	
	/**
	 * Select the deck cards according to cards displayed in the card labels
	 */
	protected void updateDeck() {
		System.out.println("calc panel update deck");
		deckPanel.deselectCards();
		for (CardButton b : cardButtons) {
			if (b.getCard() != null) {
				deckPanel.setCardSelected(b.getCard(), true);
			}
		}
	}
	
	/**
	 * get unselected deck cards.
	 * always returns a new list
	 */
	protected List<String> getDeck() {
		return deckPanel.getCards(false);
	}
	
	/** display the given board, cards and hand value type */
	public void displayHand(String[] board, List<String[]> holeCards, String type) {
		// TODO - add blockers
		clear();
		if (board != null) {
			// board panel could be null...
			boardCardPanel.setCards(Arrays.asList(board));
		}
		for (int n = 0; n < holeCards.size(); n++) {
			handCardPanels[n].setCards(Arrays.asList(holeCards.get(n)));
		}
		randNumOppSpinner.setValue(holeCards.size());
		updateDeck();
		PokerItem.select(pokerCombo, type);
	}
	
	/**
	 * get cards from array of hand card panels.
	 * return null if no cards set or some hands incomplete
	 */
	private void collectCards(List<String[]> hands, List<HandCardPanel> panels) {
		for (HandCardPanel p : handCardPanels) {
			List<String> cards = p.getCards();
			if (cards.size() > 0) {
				if (cards.size() < p.getMinCards()) {
					System.out.println("not enough cards for " + p);
					panels.clear();
					hands.clear();
				}
				hands.add(cards.toArray(new String[cards.size()]));
				panels.add(p);
			}
		}
		System.out.println("hands: " + hands.size());
	}
	
	/** calc button pressed */
	protected void calc() {
		for (HandCardPanel hp : handCardPanels) {
			hp.setEquity(null);
		}
		
		List<String> board = boardCardPanel != null ? boardCardPanel.getCards() : null;
		List<HandCardPanel> cardPanels = new ArrayList<>();
		List<String[]> cards = new ArrayList<>();
		collectCards(cards, cardPanels);
		
		if (cards.size() == 0) {
			System.out.println("no hands");
			return;
		}
		
		final List<String> blockers = blockersCardPanel.getCards();
		final PokerItem pokerItem = (PokerItem) pokerCombo.getSelectedItem();
		int draws = 0;
		if (drawsSpinner != null) {
			draws = ((SpinnerNumberModel) drawsSpinner.getModel()).getNumber().intValue();
		}
		
		final MEquity[] meqs = pokerItem.poker.equity(board, cards, blockers, draws);
		
		for (int n = 0; n < meqs.length; n++) {
			cardPanels.get(n).setEquity(meqs[n]);
		}
		
	}
	
	/** hide the cards in the deck. subclass should also hide opponents hole card */
	protected void hideOpp(boolean hide) {
		for (int n = 1; n < handCardPanels.length; n++) {
			handCardPanels[n].setCardsHidden(hide);
		}
	}
	
	/** random button pressed */
	protected void random(int num) {
		String[] deck = Poker.deck();
		ArrayUtil.shuffle(deck, new Random());
		for (int n = 0; n < num; n++) {
			int c = handCardPanels[n].getMaxCards();
			handCardPanels[n].setCards(Arrays.asList(Arrays.copyOfRange(deck, n * c, n * c + c)));
		}
		updateDeck();
	}
	
}
