package pet.ui;

import java.util.Locale;

import javax.swing.*;

import pet.hp.Hand;
import pet.hp.History;
import pet.hp.impl.PSParser;
import pet.hp.info.FollowThread;
import pet.hp.info.Info;
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
		// os x assumes US locale if system language is english...
		String lang = System.getenv("LANG");
		if (lang != null && lang.contains("en_") && !lang.contains("en_US")) {
			Locale.setDefault(Locale.UK);
		}
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
				// due to the java console panel
				instance = new PokerFrame();
				instance.setVisible(true);
			}
		});
		
	}
	
	/** all the parsed data */
	private final History history = new History();
	/** thread that feeds the parser */
	private final FollowThread followThread = new FollowThread(new PSParser(history));
	/** data analysis */
	private final Info info = new Info(history);
	private final JTabbedPane tabs = new JTabbedPane();
	private final ReplayPanel replayPanel = new ReplayPanel();
	private final BankrollPanel bankrollPanel = new BankrollPanel();
	private final HUDPanel hudPanel = new HUDPanel();
	private final HistoryPanel historyPanel = new HistoryPanel();
	private final HandsPanel handsPanel = new HandsPanel();
	private final HoldemCalcPanel holdemPanel = new HoldemCalcPanel(false);
	private final HoldemCalcPanel omahaPanel = new HoldemCalcPanel(true);
	private final DrawCalcPanel drawPanel = new DrawCalcPanel();
	private final GamesPanel gamesPanel = new GamesPanel();
	
	public PokerFrame() {
		super("Poker Equity Tool");
		
		tabs.addTab("Hold'em", holdemPanel);
		tabs.addTab("Omaha", omahaPanel);
		tabs.addTab("Draw", drawPanel);
		tabs.addTab("History", historyPanel);
		tabs.addTab("Players", new PlayerPanel());
		tabs.addTab("Games", gamesPanel);
		tabs.addTab("Tourns", new TournPanel());
		tabs.addTab("Graph", bankrollPanel);
		tabs.addTab("Hands", handsPanel);
		tabs.addTab("Replay", replayPanel);
		tabs.addTab("HUD", hudPanel);
		
		history.addListener(hudPanel);
		history.addListener(gamesPanel);
		
		followThread.addListener(historyPanel);
		followThread.start();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(tabs);
		pack();
	}
	
	public Info getInfo() {
		return info;
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
	public void displayHud(Hand hand) {
		hudPanel.showHand(hand);
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

	public void displayHands(long tournid) {
		handsPanel.displayHands(tournid);
		tabs.setSelectedComponent(handsPanel);
	}
	
	public void displayHoldemEquity(String[] board, String[][] holes, boolean omaha, boolean hilo) {
		HoldemCalcPanel panel = omaha ? omahaPanel : holdemPanel;
		panel.displayHand(board, holes, hilo);
		tabs.setSelectedComponent(panel);
	}
	
	public void displayDrawEquity(String[][] holes) {
		drawPanel.displayHand(holes);
		tabs.setSelectedComponent(drawPanel);
	}

}
