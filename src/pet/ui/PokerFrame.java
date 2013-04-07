package pet.ui;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.*;

import pet.PET;
import pet.hp.*;
import pet.hp.info.*;
import pet.ui.eq.*;
import pet.ui.graph.GraphData;
import pet.ui.hp.*;
import pet.ui.hud.HUDManager;
import pet.ui.replay.ReplayPanel;

/**
 * Poker equity GUI tool.
 */
public class PokerFrame extends JFrame {
	
	// left triangle 25c0, right triangle 25b6 
	public static final String LEFT_TRI = "\u25c0";
	public static final String RIGHT_TRI = "\u25b6";
	public static final Font boldfont = new Font("SansSerif", Font.BOLD, 12);
	public static final Font bigfont = new Font("SansSerif", Font.BOLD, 24);
	
	/** data analysis */
	// TODO should move to PET
	private final Info info = new Info();
	
	private final JTabbedPane tabs = new JTabbedPane();
	private final JTabbedPane eqTabs = new JTabbedPane();
	private final JTabbedPane hisTabs = new JTabbedPane();
	private final ReplayPanel replayPanel = new ReplayPanel();
	private final BankrollPanel bankrollPanel = new BankrollPanel();
	private final LastHandPanel lastHandPanel = new LastHandPanel();
	private final FilesPanel filesPanel = new FilesPanel();
	private final HandsPanel handsPanel = new HandsPanel();
	private final HoldemCalcPanel holdemPanel = new HoldemCalcPanel("Hold'em", 1, 2);
	private final HoldemCalcPanel omahaPanel = new HoldemCalcPanel("Omaha", 2, 4);
	private final HoldemCalcPanel omaha5Panel = new HoldemCalcPanel("5-Omaha", 2, 5);
	private final DrawCalcPanel drawPanel = new DrawCalcPanel();
	private final BadugiCalcPanel badugiPanel = new BadugiCalcPanel();
	private final GamesPanel gamesPanel = new GamesPanel();
	private final PlayerPanel playerPanel = new PlayerPanel();
	private final HUDManager hudManager = new HUDManager();
	private final AboutPanel aboutPanel = new AboutPanel();
	private final StudCalcPanel studPanel = new StudCalcPanel();
	
	public PokerFrame() {
		super("Poker Equity Tool");
		try (InputStream iconIs = getClass().getResourceAsStream("/pet32.png")) {
			BufferedImage icon = ImageIO.read(iconIs);
		    setIconImage(icon);
		    //com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
		    //app.setDockIconImage (icon);
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		tabs.addTab("Equity", eqTabs);
		tabs.addTab("History", hisTabs);
		tabs.addTab("About", aboutPanel);
		
		eqTabs.addTab("Hold'em", holdemPanel);
		eqTabs.addTab("Omaha", omahaPanel);
		eqTabs.addTab("5-Omaha", omaha5Panel);
		eqTabs.addTab("Draw", drawPanel);
		eqTabs.addTab("Badugi", badugiPanel);
		eqTabs.addTab("Stud", studPanel);
		
		hisTabs.addTab("Files", filesPanel);
		hisTabs.addTab("Players", playerPanel);
		hisTabs.addTab("Games", gamesPanel);
		hisTabs.addTab("Tournaments", new TournPanel());
		hisTabs.addTab("Graph", bankrollPanel);
		hisTabs.addTab("Hands", handsPanel);
		hisTabs.addTab("Replay", replayPanel);
		hisTabs.addTab("Last Hand", lastHandPanel);
		
		PET.getHistory().addListener(lastHandPanel);
		PET.getHistory().addListener(gamesPanel);
		PET.getHistory().addListener(info);
		PET.getHistory().addListener(hudManager);
		
		// TODO
//		followThread.addListener(historyPanel);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(tabs);
		// XXX can cause infinite loop in jeditorpane layout
		pack();
	}
	
	public Info getInfo() {
		return info;
	}
	
	/** display hand in replayer */
	public void replayHand(Hand hand) {
		replayPanel.setHand(hand);
		hisTabs.setSelectedComponent(replayPanel);
		tabs.setSelectedComponent(hisTabs);
	}
	
	/** display hand in hand panel */
	public void displayHand(Hand hand) {
		lastHandPanel.showHand(hand);
		hisTabs.setSelectedComponent(lastHandPanel);
		tabs.setSelectedComponent(hisTabs);
	}
	
	public void displayBankRoll(GraphData bankRoll) {
		bankrollPanel.setData(bankRoll);
		hisTabs.setSelectedComponent(bankrollPanel);
		tabs.setSelectedComponent(hisTabs);
	}
	
	/** display hands in hands tab */
	public void displayHands(String name, String gameid) {
		handsPanel.displayHands(name, gameid);
		hisTabs.setSelectedComponent(handsPanel);
		tabs.setSelectedComponent(hisTabs);
	}

	public void displayHands(long tournid) {
		handsPanel.displayHands(tournid);
		hisTabs.setSelectedComponent(handsPanel);
		tabs.setSelectedComponent(hisTabs);
	}
	
	/**
	 * display the calc panel for the game type and return it so you can set the
	 * hand
	 */
	public CalcPanel displayCalcPanel(Game.Type gameType) {
		CalcPanel p = getCalcPanel(gameType);
		eqTabs.setSelectedComponent(p);
		tabs.setSelectedComponent(eqTabs);
		return p;
	}
	
	private CalcPanel getCalcPanel(Game.Type gameType) {
		switch (gameType) {
			case HE: 
				return holdemPanel;
			case OM5:
			case OM51:
			case OM51HL:
			case OM5HL:
			case OM:
			case OMHL:
				return omahaPanel;
			case FSTUD:
			case AFTD:
			case DSTD:
			case FCD:
			case DSSD: 
				return drawPanel;
			case STUD:
			case STUDHL:
			case RAZZ:
				return studPanel;
			case BG:
				return badugiPanel;
			default:
				throw new RuntimeException("no panel for game " + gameType);
		}
	}

	public void displayPlayer(String player) {
		playerPanel.displayPlayer(player);
		hisTabs.setSelectedComponent(playerPanel);
		tabs.setSelectedComponent(hisTabs);
	}

	public HUDManager getHudManager() {
		return hudManager;
	}
	
}
