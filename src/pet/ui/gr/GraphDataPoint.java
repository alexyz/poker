package pet.ui.gr;

public class GraphDataPoint<T> {
	public final T ref;
	public final int x;
	public final int y;

	public GraphDataPoint(T ref, int x, int y) {
		this.ref = ref;
		this.x = x;
		this.y = y;
	}

}
