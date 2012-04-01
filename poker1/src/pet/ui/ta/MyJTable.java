package pet.ui.ta;

import java.awt.Component;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

public class MyJTable<T> extends JTable {
	public MyJTable(MyTableModel<T> model) {
		super(model);
	}
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int r, int c) {
		int r2 = convertRowIndexToModel(r);
		Component comp = super.prepareRenderer(renderer, r, c);
		if (comp instanceof JComponent) {
			JComponent jc = (JComponent)comp;
			// FIXME calcs all tooltops
			jc.setToolTipText(getModel().getToolTip(r2, c));
		}
		return comp;
	}
	@Override
	public MyTableModel<T> getModel() {
		return (MyTableModel<T>) super.getModel();
	}
}
