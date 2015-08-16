package pet.ui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

/**
 * JTable that uses a MyTableModel to get colour and tool tip information and
 * provides configurable columns
 */
public class MyJTable extends JTable {
	
	public static final Color tableBackgroundColour = UIManager.getDefaults().getColor("Table.background");
	public static final Font tableFont = UIManager.getDefaults().getFont("Table.font");
	public static final Font monoTableFont = new Font("Monospaced", 0, tableFont.getSize());
	public static final Font boldTableFont = tableFont.deriveFont(Font.BOLD);
	
	public MyJTable() {
		getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				popup(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				popup(e);
			}
		});
		// sun's default is 450*400 which makes app too tall
		setPreferredScrollableViewportSize(new Dimension(100, 100));
	}
	
	/** show configurable column menu */
	private void popup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			System.out.println("header clicked");
			JPopupMenu menu = new JPopupMenu("Columns");
			final MyTableModel<?> model = (MyTableModel<?>) getModel();
			List<? extends MyColumn<?>> allcols = model.getAllColumns();
			for (int c = 0; c < allcols.size(); c++) {
				MyColumn<?> col = allcols.get(c);
				JCheckBoxMenuItem cb = new JCheckBoxMenuItem(col.name);
				cb.setSelected(model.getAllColumn(c));
				final int fc = c;
				cb.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						model.setAllColumn(fc);
					}
				});
				menu.add(cb);
			}
			menu.show(getTableHeader(), e.getX(), e.getY());
		}
	}
	
	@Override
	public String getToolTipText(MouseEvent e) {
		int r = rowAtPoint(e.getPoint());
		int c = columnAtPoint(e.getPoint());
		if (r >= 0 && c >= 0) {
			// calc tooltip on demand
			int r2 = convertRowIndexToModel(r);
			int c2 = convertColumnIndexToModel(c);
			return ((MyTableModel<?>)getModel()).getToolTip(r2, c2);
		} else {
			return null;
		}
	}
	
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
		Component comp = super.prepareRenderer(renderer, row, col);
		if (comp instanceof JComponent) {
			JComponent jcomp = (JComponent)comp;
			// need to convert before sending to table model
			int row2 = convertRowIndexToModel(row);
			int col2 = convertColumnIndexToModel(col);
			if (!getSelectionModel().isSelectedIndex(row)) {
				Color colour = ((MyTableModel<?>)getModel()).getColour(row2, col2);
				jcomp.setBackground(colour != null ? colour : tableBackgroundColour);
				//jcomp.setFont(new Font("Monospaced", 0, 8));
			}
			Font font = ((MyTableModel<?>)getModel()).getFont(row2, col2);
			jcomp.setFont(font != null ? font : tableFont);	
		}
		return comp;
	}
	
}
