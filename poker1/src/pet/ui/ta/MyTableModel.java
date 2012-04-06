package pet.ui.ta;

import java.awt.Color;
import java.awt.Font;
import java.util.*;

import javax.swing.table.AbstractTableModel;

public class MyTableModel<T> extends AbstractTableModel {
	
	private final List<MyTableModelColumn<T,?>> cols = new ArrayList<MyTableModelColumn<T,?>>();
	
	private final List<T> rows = new ArrayList<T>();
	
	public MyTableModel(List<MyTableModelColumn<T,?>> cols) {
		this.cols.addAll(cols);
	}
	
	/**
	 * Get the population T for the given T
	 */
	public T getPopulation(T row) {
		return null;
	}
	
	public void setRows(Collection<T> rows) {
		this.rows.clear();
		this.rows.addAll(rows);
		fireTableStructureChanged();
		// doesn't seem to work
		fireTableDataChanged();
		//System.out.println("table model rows now " + rows);
	}
	
	public T getRow(int r) {
		return rows.get(r);
	}
	
	@Override
	public int getColumnCount() {
		return cols.size();
	}
	
	@Override
	public String getColumnName(int c) {
		return cols.get(c).name;
	}

	@Override
	public int getRowCount() {
		//System.out.println("get rows " + this.getClass() + " => " + rows.size());
		return rows.size();
	}
	
	@Override
	public Class<?> getColumnClass(int c) {
		return cols.get(c).cl;
	}

	@Override
	public Object getValueAt(int r, int c) {
		if (r < rows.size()) {
			T row = rows.get(r);
			return cols.get(c).getValue(row);
		}
		return null;
	}

	public String getToolTip(int r, int c) {
		//System.out.println("get tool tip for row " + r);
		if (r < rows.size()) {
			T row = rows.get(r);
			//System.out.println("row is " + row);
			MyTableModelColumn<T, ?> col = cols.get(c);
			StringBuilder sb = new StringBuilder();
			sb.append("<html><b>").append(col.desc).append("</b>");
			sb.append("<br>").append(col.getValue(row));
			T population = getPopulation(row);
			if (population != null) {
				sb.append("<br><i>").append(col.getPopValue(population)).append(" (population)</i>");
			}
			sb.append("<br>(").append(r).append(",").append(c).append(")");
			sb.append("</html>");
			return sb.toString();
		}
		return null;
	}
	
	public Color getColour(int r, int c) {
		if (r < rows.size()) {
			T row = rows.get(r);
			//System.out.println("row is " + row);
			MyTableModelColumn<T, ?> col = cols.get(c);
			return col.getColour(row);
		}
		return null;
	}
	
	public Font getFont(int r, int c) {
		if (r < rows.size()) {
			T row = rows.get(r);
			//System.out.println("row is " + row);
			MyTableModelColumn<T, ?> col = cols.get(c);
			return col.getFont(row);
		}
		return null;
	}
	
}
