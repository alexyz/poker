package pet.ui.eq;

import pet.eq.Poker;

/** represents a poker valuation type in the combo box */
public class PokerItem {
	public static final String HIGH = "High", AFLOW = "A-5 Low", DSLOW = "2-7 Low", HILO = "High/A-5 Low (8)";
	
	public final String name;
	public final Poker poker;
	
	public PokerItem(String name, Poker poker) {
		this.name = name;
		this.poker = poker;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
