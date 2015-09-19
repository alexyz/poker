package pet.ui.eq;

import java.awt.*;

import javax.swing.*;

import pet.eq.*;

/**
 * toggle button to show blank or specific card.
 */
class CardButton extends JToggleButton {
	
	public static final Font cardfont = new Font("SansSerif", Font.PLAIN, 24);
	
	private String card;
	private boolean hidden;
	
	/**
	 * create a blank card button
	 */
	public CardButton() {
		super("  ");
		setFont(cardfont);
		setPreferredSize(new Dimension(48, 32));
		setMinimumSize(getPreferredSize());
		setBackground(Color.white);
	}

	@Override
	public Insets getInsets() {
		// override the crazy default insets that make it impossible to show big
		// text in a small button
		return new Insets(2, 2, 2, 2);
	}
	
	@Override
	public Insets getInsets(Insets insets) {
		return getInsets();
	}
	
	public void setCard(String card) {
		//System.out.println("card button " + getName() + " set card " + card);
		this.card = card;
		update();
	}
	
	public String getCard() {
		return card;
	}
	
	public void setCardHidden(boolean hidden) {
		this.hidden = hidden;
		update();
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	private void update() {
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
	}
	
}
