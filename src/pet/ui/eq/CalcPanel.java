package pet.ui.eq;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;

/**
 * displays a deck and the game specific calc panel
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
	 * the list of card buttons used by the board, player hole cards and blockers
	 */
	private final List<CardButton> cardButtons = new ArrayList<CardButton>();
	
	private HandCardPanel[] cardPanels;
	private CardPanel boardPanel;

	public CalcPanel() {
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
		
		deckPanel.addPropertyChangeListener(DeckPanel.CARD_SEL_PROP_CHANGE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				System.out.println("calc panel deck card selected prop change");
				// set the selected card to the deck card and move to next card
				for (int n = 0; n < cardButtons.size(); n++) {
					CardButton b = cardButtons.get(n);
					if (b.isSelected()) {
						String oldc = b.getCard();
						String newc = (String) e.getNewValue();
						if (oldc != null && !oldc.equals(newc)) {
							deckPanel.setCardSelected(oldc, false);
						}
						b.setCard(newc);
						b.setSelected(false);
						
						// select next button
						CardButton nb = cardButtons.get((n + 1) % cardButtons.size());
						nb.setSelected(true);
						// req focus? will take it from deck
						break;
					}
				}
				// FIXME if no card selected, set first (or reject)
			}
		});
		
		deckPanel.addPropertyChangeListener(DeckPanel.CARD_DESEL_PROP_CHANGE, new PropertyChangeListener() {
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
	protected void setHandCardPanels(HandCardPanel[] cardPanels) {
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
			for (CardButton b : boardPanel.getCardButtons()) {
				cardButtons.add(b);
			}
		}
		
		for (CardPanel cp : cardPanels) {
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
			b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
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
			});
			b.addKeyListener(new KeyAdapter() {
				char c1 = 0;
				@Override
				public void keyTyped(KeyEvent e) {
					// TODO also detect backspace, delete, return (next row/board if blank)
					char c2 = e.getKeyChar();
					if (c1 != 0) {
						String c = new String(new char[] { 
								Character.toUpperCase(c1), 
								Character.toLowerCase(c2) 
						});
						System.out.println("-- typed " + c + " --");
						
						// note: may already be selected in deck
						if (deckPanel.setCardSelected(c, true)) {
							// unset card from any other buttons
							for (CardButton ob : cardButtons) {
								if (ob != b && ob.getCard() != null && ob.getCard().equals(c)) {
									ob.setCard(null);
									break;
								}
							}
							
							// set the card on this button
							b.setCard(c);
							b.setSelected(false);
							
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
			});
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
		if (boardPanel != null) {
			boardPanel.clearCards();
			n += boardPanel.getCardButtons().size();
		}
		for (CardPanel cp : cardPanels) {
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
	
	/**
	 * get blockers
	 */
	protected List<String> getBlockers() {
		return blockersCardPanel.getCards();
	}
	
	/** set the board, hands and blockers */
	protected void displayHand(String[] board, List<String[]> holeCards) {
		clear();
		if (board != null) {
			boardPanel.setCards(Arrays.asList(board));
		}
		for (int n = 0; n < holeCards.size(); n++) {
			cardPanels[n].setCards(Arrays.asList(holeCards.get(n)));
		}
		randNumOppSpinner.setValue(holeCards.size());
		updateDeck();
	}
	
	/**
	 * get cards from array of hand card panels.
	 * return null if no cards set or some hands incomplete
	 */
	public void collectCards(List<String[]> hands, List<HandCardPanel> panels) {
		for (HandCardPanel p : cardPanels) {
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
