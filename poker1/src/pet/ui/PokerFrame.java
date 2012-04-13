package pet.ui;

import java.util.Locale;

import javax.swing.*;

import pet.hp.Hand;
import pet.hp.impl.PSParser;
import pet.hp.info.FollowThread;
import pet.hp.info.History;
import pet.ui.eq.*;
import pet.ui.gr.GraphData;
import pet.ui.rep.ReplayPanel;

/**
 * Poker equity GUI tool.
 */
public class PokerFrame extends JFrame {
	
	// left triangle 25c0, right triangle 25b6 
	public static final String LEFT_TRI = "\u25c0";
	public static final String RIGHT_TRI = "\u25b6";
	
	private static PokerFrame instance;
	
	public static PokerFrame getInstance() {
		return instance;
	}
	
	public static void main(String[] args) {
		Locale.setDefault(Locale.UK);
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		ToolTipManager.sharedInstance().setDismissDelay(5000);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// need to create and pack in awt thread otherwise it can deadlock
				instance = new PokerFrame();
				instance.setVisible(true);
			}
		});
		
	}
	
	private final FollowThread followThread = new FollowThread(new PSParser());
	private final History history = new History();
	
	private final JTabbedPane tabs = new JTabbedPane();
	private final ReplayPanel replayPanel = new ReplayPanel();
	private final BankrollPanel bankrollPanel = new BankrollPanel();
	private final HUDPanel hudPanel = new HUDPanel();
	private final HistoryPanel historyPanel = new HistoryPanel();
	private final HandsPanel handsPanel = new HandsPanel();
	
	public PokerFrame() {
		super("Poker Equity Tool");
		
		tabs.addTab("Hold'em", new HoldemCalcPanel(true));
		tabs.addTab("Omaha", new HoldemCalcPanel(false));
		tabs.addTab("Draw", new DrawCalcPanel());
		tabs.addTab("History", historyPanel);
		tabs.addTab("Players", new PlayerPanel());
		tabs.addTab("Graph", bankrollPanel);
		tabs.addTab("Hands", handsPanel);
		tabs.addTab("Replay", replayPanel);
		tabs.addTab("HUD", hudPanel);
		
		followThread.addListener(hudPanel);
		followThread.addListener(historyPanel);
		followThread.addListener(history);
		followThread.start();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(tabs);
		pack();
	}
	
	public History getHistory() {
		return history;
	}
	
	/** display hand in replayer */
	public void replayHand(Hand hand) {
		replayPanel.setHand(hand);
		tabs.setSelectedComponent(replayPanel);
	}
	
	/** display hand in hud */
	public void hudHand(Hand hand) {
		hudPanel.nextHand(hand, true);
		tabs.setSelectedComponent(hudPanel);
	}

	public void displayBankRoll(GraphData bankRoll) {
		bankrollPanel.setData(bankRoll);
		tabs.setSelectedComponent(bankrollPanel);
	}
	
	public FollowThread getFollow() {
		return followThread;
	}
	
	/** display hands in hands tab */
	public void displayHands(String name, String gameid) {
		handsPanel.displayHands(name, gameid);
		tabs.setSelectedComponent(handsPanel);
	}

}
