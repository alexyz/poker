package pet.ui;

import javax.swing.*;

import pet.ui.eq.*;

/**
 * Poker equity GUI tool.
 * TODO
 * back and fwd button
 * resize listener to reduce card font size and eq label size
 * 
 */
public class PokerFrame extends JFrame {

	public static void main(String[] args) {
		JFrame f = new PokerFrame();
		f.setVisible(true);
	}

	public PokerFrame() {
		super("Poker Equity Tool");
		JTabbedPane p = new JTabbedPane();
		p.addTab("Hold'em", new HoldemCalcPanel(true));
		p.addTab("Omaha", new HoldemCalcPanel(false));
		p.addTab("Draw", new DrawCalcPanel());
		p.addTab("History", new HistoryPanel());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(p);
		pack();
		ToolTipManager.sharedInstance().setDismissDelay(60000);
	}

}
