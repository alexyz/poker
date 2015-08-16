package pet.ui.hp;

import java.awt.Font;

import javax.swing.table.DefaultTableCellRenderer;

import pet.eq.PokerUtil;

class HandRenderer extends DefaultTableCellRenderer {
	public HandRenderer() {
		Font f = getFont();
		setFont(new Font("Monospaced", Font.PLAIN, f.getSize()));
	}
	@Override
	protected void setValue(Object value) {
		if (value instanceof String[]) {
			value = PokerUtil.cardsString((String[]) value);
		}
		super.setValue(value);
	}
}