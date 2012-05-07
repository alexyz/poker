package pet.ui.gr;

import java.util.*;

public class GraphData {
	
	public String name;
	
	public final List<GraphDataPoint> points = new ArrayList<GraphDataPoint>();

	public String getXName(int x) {
		return String.valueOf(x);
	}

	public String getYName(int y) {
		return String.valueOf(y);
	}
	
	
}