package pet.ui;

import java.util.Locale;

import javax.swing.*;

import pet.hp.Hand;
import pet.hp.util.History;
import pet.ui.eq.*;
import pet.ui.gr.GraphData;
import pet.ui.rep.ReplayPanel;

/**
 * Poker equity GUI tool.
 */
public class PokerFrame extends JFrame {
	
	private static final PokerFrame instance = new PokerFrame();
	
	public static PokerFrame getInstance() {
		return instance;
	}
	
	public static void main(String[] args) {
		Locale.setDefault(Locale.UK);
		instance.setVisible(true);
	}
	
	private final History history = new History();
	private final JTabbedPane tabs = new JTabbedPane();
	private final ReplayPanel replayPanel = new ReplayPanel();
	private final BankrollPanel bankrollPanel = new BankrollPanel();

	public PokerFrame() {
		super("Poker Equity Tool");
		
		tabs.addTab("Hold'em", new HoldemCalcPanel(true));
		tabs.addTab("Omaha", new HoldemCalcPanel(false));
		tabs.addTab("Draw", new DrawCalcPanel());
		tabs.addTab("History", new HistoryPanel());
		tabs.addTab("Players", new PlayerPanel());
		tabs.addTab("Bankroll", bankrollPanel);
		tabs.addTab("Session", new SessionPanel());
		tabs.addTab("Replay", replayPanel);
		tabs.addTab("Console", new ConsolePanel());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(tabs);
		pack();
		ToolTipManager.sharedInstance().setDismissDelay(60000);
	}
	
	public History getHistory() {
		return history;
	}
	
	/** display hand in replayer */
	public void displayHand(Hand hand) {
		replayPanel.setHand(hand);
		tabs.setSelectedComponent(replayPanel);
	}

	public void displayBankRoll(GraphData bankRoll) {
		bankrollPanel.setData(bankRoll);
		tabs.setSelectedComponent(bankrollPanel);
	}

}
