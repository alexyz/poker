package pet.ui.ta;

import java.awt.event.MouseEvent;

import javax.swing.*;

// TODO type param should really be table model, not record
public class MyJTable<T> extends JTable {
	public MyJTable(MyTableModel<T> model) {
		super(model);
	}
	@Override
	public String getToolTipText(MouseEvent e) {
		int r = rowAtPoint(e.getPoint());
		int c = columnAtPoint(e.getPoint());
		if (r >= 0 && c >= 0) {
			// calc tooltip on demand
			int r2 = convertRowIndexToModel(r);
			return getModel().getToolTip(r2, c);
		} else {
			return null;
		}
	}
	/*
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
	*/
	@Override
	public MyTableModel<T> getModel() {
		return (MyTableModel<T>) super.getModel();
	}
}
