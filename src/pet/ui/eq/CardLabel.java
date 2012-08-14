package pet.ui.eq;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import pet.eq.*;

/**
 * label to show blank or individual card. the name property of the component is the card
 */
class CardLabel extends JLabel {
	
	public static final Font cardfont = new Font("SansSerif", Font.PLAIN, 24);
	public static String CARD_SEL_PROP_CHANGE = "cardsel", CARD_DESEL_PROP_CHANGE = "carddesel";
	
	/**
	 * get the cards for the card labels that have cards (array could have 0 to
	 * cardLabels.size elements)
	 */
	public static String[] getCards(List<CardLabel> cardLabels) {
		List<String> cards = new ArrayList<String>();
		for (CardLabel cl : cardLabels) {
			if (cl.getCard() != null) {
				cards.add(cl.getCard());
			}
		}
		return cards.toArray(new String[cards.size()]);
	}
	
	private boolean selected;
	private boolean hidden;
	
	/**
	 * create a blank card label
	 */
	public CardLabel() {
		super("  ");
		setFont(cardfont);
		setPreferredSize(new Dimension(48, 32));
		setMinimumSize(getPreferredSize());
		setOpaque(true);
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
		setBackground(Color.white);
		setBorder(BorderFactory.createRaisedBevelBorder());
		setFocusable(true);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setBackground(Color.yellow);
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("card label mouse clicked");
				if (!hidden) {
					//setCardSelected(!selected);
					firePropertyChange(!selected ? CARD_SEL_PROP_CHANGE : CARD_DESEL_PROP_CHANGE, "", getName());
					update();
				}
			}
			@Override
			public void mouseExited(MouseEvent e) {
				//setBackground(selected && !hidden ? Color.gray : Color.white);
				update();
			}
		});
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == ' ') {
					if (!hidden) {
						//setCardSelected(!selected);
						firePropertyChange(!selected ? CARD_SEL_PROP_CHANGE : CARD_DESEL_PROP_CHANGE, "", getName());
					}
				}
			}
		});
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				update();
			}
			@Override
			public void focusLost(FocusEvent e) {
				update();
			}
		});
		setCard(null);
	}
	/** set the selected flag and update the appearance */
	public void setCardSelected(boolean sel) {
		System.out.println("card label " + getName() + " set selected " + sel);
		selected = sel;
		update();
	}
	public void setCard(String c) {
		setName(c);
		update();
	}
	private void update() {
		String card = getName();
		if (card != null) {
			if (hidden) {
				setForeground(Color.black);
				setText("--");
			} else {
				setForeground(PokerUtil.suitColour(Poker.suit(card)));
				char f = Poker.face(card);
				char s = PokerUtil.suitSymbol(card, true);
				// doesn't fit
				//setText(f == 'T' ? "10" + s : "" + f + s);
				setText("" + f + s);
			}
		} else {
			setText("  ");
		}
		if (selected && !hidden) {
			setBackground(isFocusOwner() ? Color.gray : Color.lightGray);
			setBorder(BorderFactory.createLoweredBevelBorder());
		} else {
			setBackground(Color.white);
			setBorder(BorderFactory.createRaisedBevelBorder());
		}
	}
	public String getCard() {
		return getName();
	}
	public boolean isCardSelected() {
		return selected;
	}
	public void setCardHidden(boolean hidden) {
		this.hidden = hidden;
		update();
	}
}