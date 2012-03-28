package pet.ui;

import java.util.Locale;

import javax.swing.*;

import pet.hp.Hand;
import pet.hp.util.History;
import pet.ui.eq.*;
import pet.ui.rep.ReplayPanel;

/**
 * Poker equity GUI tool.
 */
public class PokerFrame extends JFrame {
	
	private static final PokerFrame instance = new PokerFrame();
	
	public static void main(String[] args) {
		Locale.setDefault(Locale.UK);
		instance.setVisible(true);
	}
	
	public static History getHistory() {
		return instance.history;
	}
	
	public static void replay(Hand hand) {
		instance.replayPanel.setHand(hand);
		instance.p.setSelectedComponent(instance.replayPanel);
	}
	
	private final JTabbedPane p = new JTabbedPane();
	private final History history = new History();
	private final ReplayPanel replayPanel = new ReplayPanel();

	public PokerFrame() {
		super("Poker Equity Tool");
		
		p.addTab("Hold'em", new HoldemCalcPanel(true));
		p.addTab("Omaha", new HoldemCalcPanel(false));
		p.addTab("Draw", new DrawCalcPanel());
		p.addTab("History", new HistoryPanel());
		p.addTab("Players", new PlayerPanel());
		p.addTab("Bankroll", new BankrollPanel());
		p.addTab("Session", new SessionPanel());
		p.addTab("Replay", replayPanel);
		p.addTab("Console", new ConsolePanel());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(p);
		pack();
		ToolTipManager.sharedInstance().setDismissDelay(60000);
	}

}
