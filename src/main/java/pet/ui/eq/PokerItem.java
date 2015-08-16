package pet.ui.eq;

import javax.swing.JComboBox;

import pet.eq.Poker;

/** represents a poker valuation type in the combo box */
public class PokerItem {
	
	public static final String HIGH = "High";
	public static final String AFLOW = "A-5 Low";
	public static final String DSLOW = "2-7 Low";
	public static final String HILO = "High/A-5 Low (8)";
	public static final String BADUGI = "Badugi";
	
	/**
	 * select the item in the combo box
	 */
	public static void select(JComboBox<PokerItem> combo, String name) {
		for (int n = 0; n < combo.getItemCount(); n++) {
			PokerItem p = combo.getItemAt(n);
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
