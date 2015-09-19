package pet.ui.hp;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import pet.ui.graph.*;

/**
 * TODO session button
 * TODO single day bankroll - TimeGraphData
 */
public class BankrollPanel extends JPanel {
	
	private final GraphComponent graph = new GraphComponent();
	
	public BankrollPanel() {
		super(new BorderLayout());
		add(graph, BorderLayout.CENTER);
	}

	public void setData(GraphData bankRoll) {
		graph.setData(bankRoll);
		revalidate();
		repaint();
	}

}
