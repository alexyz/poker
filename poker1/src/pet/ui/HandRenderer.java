package pet.ui;

import java.awt.Font;

import javax.swing.table.DefaultTableCellRenderer;

import pet.eq.Poker;

class HandRenderer extends DefaultTableCellRenderer {
	public HandRenderer() {
		Font f = getFont();
		setFont(new Font("Monospaced", Font.PLAIN, f.getSize()));
	}
	@Override
	protected void setValue(Object value) {
		if (value instanceof String[]) {
			value = Poker.toString((String[]) value);
		}
		super.setValue(value);
	}
}