package pet.ui.ta;

public abstract class MyTableModelColumn<T,S> {
	public final String name;
	public final Class<?> cl;
	public final String desc;

	public MyTableModelColumn(String name, String desc, Class<S> cl) {
		this.name = name;
		this.desc = desc;
		this.cl = cl;
	}
	
	public S getPopValue(T o) {
		return getValue(o);
	}
	
	public abstract S getValue(T o);
}
