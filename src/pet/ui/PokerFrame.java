package pet.ui;

import java.awt.Font;
import java.util.Locale;

import javax.swing.*;

import pet.hp.*;
import pet.hp.impl.PSParser;
import pet.hp.info.*;
import pet.ui.eq.*;
import pet.ui.gr.GraphData;
import pet.ui.hud.HUDManager;
import pet.ui.rep.ReplayPanel;

/**
 * Poker equity GUI tool.
 */
public class PokerFrame extends JFrame {
	
	// left triangle 25c0, right triangle 25b6 
	public static final String LEFT_TRI = "\u25c0";
	public static final String RIGHT_TRI = "\u25b6";
	public static final Font boldfont = new Font("SansSerif", Font.BOLD, 12);
	public static final Font bigfont = new Font("SansSerif", Font.BOLD, 24);
	
	private static PokerFrame instance;
	
	public static PokerFrame getInstance() {
		return instance;
	}
	
	public static void main(String[] args) {
		// os x assumes US locale if system language is english...
		// user needs to add british english to list of languages in system preferences/language and text
		System.out.println("locale " + Locale.getDefault());
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
				System.out.println("Poker Equity Tool - https://github.com/alexyz");
				instance.start();
				instance.setVisible(true);
			}
		});
		
	}
	
	/** all the parsed data */
	private final History history = new History();
	/** thread that feeds the parser */
	private final FollowThread followThread = new FollowThread(new PSParser(history));
	/** data analysis */
	private final Info info = new Info();
	private final JTabbedPane tabs = new JTabbedPane();
	private final ReplayPanel replayPanel = new ReplayPanel();
	private final BankrollPanel bankrollPanel = new BankrollPanel();
	private final LastHandPanel lastHandPanel = new LastHandPanel();
	private final HistoryPanel historyPanel = new HistoryPanel();
	private final HandsPanel handsPanel = new HandsPanel();
	private final HoldemCalcPanel holdemPanel = new HoldemCalcPanel(false);
	private final HoldemCalcPanel omahaPanel = new HoldemCalcPanel(true);
	private final DrawCalcPanel drawPanel = new DrawCalcPanel();
	private final GamesPanel gamesPanel = new GamesPanel();
	private final PlayerPanel playerPanel = new PlayerPanel();
	private final HUDManager hudManager = new HUDManager();
	
	public PokerFrame() {
		super("Poker Equity Tool");
		
		tabs.addTab("Hold'em", holdemPanel);
		tabs.addTab("Omaha", omahaPanel);
		tabs.addTab("Draw", drawPanel);
		tabs.addTab("History", historyPanel);
		tabs.addTab("Players", playerPanel);
		tabs.addTab("Games", gamesPanel);
		tabs.addTab("Tourns", new TournPanel());
		//tabs.addTab("Graph", bankrollPanel);
		tabs.addTab("Hands", handsPanel);
		tabs.addTab("Replay", replayPanel);
		tabs.addTab("Last Hand", lastHandPanel);
		
		history.addListener(lastHandPanel);
		history.addListener(gamesPanel);
		history.addListener(info);
		history.addListener(hudManager);
		
		followThread.addListener(historyPanel);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(tabs);
		pack();
	}
	
	public void start() {
		followThread.start();
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
	
	/** display hand in hand panel */
	public void displayHand(Hand hand) {
		lastHandPanel.showHand(hand);
		tabs.setSelectedComponent(lastHandPanel);
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

	public void displayPlayer(String player) {
		playerPanel.displayPlayer(player);
		tabs.setSelectedComponent(playerPanel);
	}

	public HUDManager getHudManager() {
		return hudManager;
	}

}
