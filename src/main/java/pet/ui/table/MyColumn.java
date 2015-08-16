package pet.ui.table;

import java.awt.Color;
import java.awt.Font;

public abstract class MyColumn<T> {
	public final String name;
	public final Class<?> cl;
	public final String desc;

	public MyColumn(Class<?> cl, String name, String desc) {
		this.name = name;
		this.desc = desc;
		this.cl = cl;
	}
	
	public Object getPopValue(T row) {
		return getValue(row);
	}
	
	public abstract Object getValue(T row);
	
	public Color getColour(T row) {
		return null;
	}
	
	public Font getFont(T row) {
		return null;
	}
	
	public String getToolTip(T row) {
		return null;
	}
}
