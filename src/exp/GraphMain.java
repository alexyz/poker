
package exp;

import java.awt.Dimension;

import javax.swing.JFrame;

import pet.ui.graph.*;

public class GraphMain {

	public static void main(String[] args) {
		GraphData data = new GraphData("sine", "x axis", "y axis") {
			@Override
			public String getXName(int x) {
				return Double.toString(x / 100.0);
			}
			@Override
			public String getYName(int y) {
				return Double.toString(y / 100.0);
			}
			@Override
			public String getXDesc(int x) {
				return "-" + x + "-";
			}
		};
		for (float x = -2; x < 2; x += 0.05f) {
			float y = (float) Math.sin(x);
			//data.points.add(new GraphDataPoint((int)(x*100.0), (int)(y*100.0), false));
		}
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GraphComponent g = new GraphComponent();
		g.setData(data);
		g.setPreferredSize(new Dimension(800,600));
		f.setContentPane(g);
		f.pack();
		f.show();
	}
	
	
}
