package pet.ui.eq;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import pet.Poker;

class CardLabel extends JLabel {
	public static final Font cardfont = new Font("SansSerif", Font.PLAIN, 24);
	public static String CARD_SEL_PROP_CHANGE = "cardsel", CARD_DESEL_PROP_CHANGE = "carddesel";
	
	/**
	 * Return colour of suit
	 */
	public static Color suitcol (char s) {
		// switch instead of map due to primitive type
		switch (s) {
		case Poker.S_SUIT: return Color.black;
		case Poker.C_SUIT: return Color.green;
		case Poker.H_SUIT: return Color.red;
		case Poker.D_SUIT: return Color.blue;
		}
		throw new RuntimeException();
	}
	
	
	private boolean selected;
	private boolean hidden;
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
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setBackground(Color.yellow);
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!hidden) {
					setCardSelected(!selected);
					firePropertyChange(selected ? CARD_SEL_PROP_CHANGE : CARD_DESEL_PROP_CHANGE, "", getName());
				}
			}
			@Override
			public void mouseExited(MouseEvent e) {
				setBackground(selected && !hidden ? Color.gray : Color.white);
			}
		});
		setCard(null);
	}
	public void setCardSelected(boolean sel) {
		selected = sel;
		update();
	}
	public CardLabel(String c) {
		this();
		setCard(c);
	}
	public void setCard(String c) {
		setName(c);
		update();
	}
	private void update() {
		String c = getName();
		if (c != null) {
			if (hidden) {
				setForeground(Color.black);
				setText("--");
			} else {
				setForeground(suitcol(Poker.suit(c)));
				char f = Poker.face(c);
				char s = Poker.suitsym(c);
				// doesn't fit
				//setText(f == 'T' ? "10" + s : "" + f + s);
				setText("" + f + s);
			}
		} else {
			setText("  ");
		}
		if (selected && !hidden) {
			setBackground(Color.gray);
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