package pet.ui;

abstract class TableModelColumn<T,S> {
	public final String name;
	public final Class<?> cl;

	public TableModelColumn(String name, Class<S> cl) {
		this.name = name;
		this.cl = cl;
	}
	
	public abstract S getValue(T o);
}