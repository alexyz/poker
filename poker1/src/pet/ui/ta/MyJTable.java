package pet.ui.ta;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

// TODO type param should really be table model, not record
public class MyJTable<T> extends JTable {
	
	public static final Color defcol = UIManager.getDefaults().getColor("Table.background");
	public static final Font deffont = UIManager.getDefaults().getFont("Table.font");
	
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
			int c2 = convertColumnIndexToModel(c);
			return getModel().getToolTip(r2, c2);
		} else {
			return null;
		}
	}
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int r, int c) {
		Component comp = super.prepareRenderer(renderer, r, c);
		if (comp instanceof JComponent) {
			JComponent jcomp = (JComponent)comp;
			// need to convert before sending to table model
			int r2 = convertRowIndexToModel(r);
			int c2 = convertColumnIndexToModel(c);
			if (!getSelectionModel().isSelectedIndex(r)) {
				Color col = getModel().getColour(r2, c2);
				jcomp.setBackground(col != null ? col : defcol);
			}
			Font font = getModel().getFont(r2, c2);
			jcomp.setFont(font != null ? font : deffont);	
		}
		return comp;
	}
	@Override
	public MyTableModel<T> getModel() {
		return (MyTableModel<T>) super.getModel();
	}
}
