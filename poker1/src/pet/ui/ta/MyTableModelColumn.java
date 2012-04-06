package pet.ui.ta;

import java.awt.Color;
import java.awt.Font;

public abstract class MyTableModelColumn<T,S> {
	public final String name;
	public final Class<?> cl;
	public final String desc;

	public MyTableModelColumn(Class<S> cl, String name, String desc) {
		this.name = name;
		this.desc = desc;
		this.cl = cl;
	}
	
	public S getPopValue(T row) {
		return getValue(row);
	}
	
	public abstract S getValue(T row);
	
	public Color getColour(T row) {
		return null;
	}
	
	public Font getFont(T row) {
		return null;
	}
}
