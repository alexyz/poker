package pet.ui.eq;

import javax.swing.JComboBox;

import pet.eq.Poker;

/** represents a poker valuation type in the combo box */
public class PokerItem {
	
	public static final String HIGH = "High", AFLOW = "A-5 Low", DSLOW = "2-7 Low", HILO = "High/A-5 Low (8)";
	
	/**
	 * select the item in the combo box
	 */
	public static void select(JComboBox combo, String name) {
		for (int n = 0; n < combo.getItemCount(); n++) {
			PokerItem p = (PokerItem) combo.getItemAt(n);
			if (p.name.equals(name)) {
				combo.setSelectedIndex(n);
				return;
			}
		}
	}
	
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
