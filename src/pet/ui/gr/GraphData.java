package pet.ui.gr;

import java.util.*;

public class GraphData {
	
	public final String name;
	public final List<GraphDataPoint> points = new ArrayList<GraphDataPoint>();
	/** x axis title */
	public String x;
	/** y axis title */
	public String y;
	
	public GraphData(String name, String x, String y) {
		this.name = name;
		this.x = x;
		this.y = y;
	}

	public String getXName(int x) {
		return String.valueOf(x);
	}
	
	/** get description of x value (which may not exist in points) */
	public String getXDesc(int x) {
		return null;
	}

	public String getYName(int y) {
		return String.valueOf(y);
	}
	
	
}
