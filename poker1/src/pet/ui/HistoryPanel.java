package pet.ui;

import java.awt.event.*;
import java.io.File;
import java.util.*;

import javax.swing.*;

import pet.HEPoker;
import pet.HandEq;
import pet.Poker;
import pet.hp.*;
import pet.hp.Action;
import pet.hp.impl.PSHP;
import pet.hp.util.FollowListener;
import pet.hp.util.FollowThread;
import pet.hp.util.HandInfo;
import pet.hp.util.History;
import pet.hp.util.PlayerInfo;
import pet.hp.util.Util;

public class HistoryPanel extends JPanel {
	
	private final JLabel pathLabel = new JLabel();
	private final JLabel progressLabel = new JLabel(" ");
	private final JButton browseButton = new JButton("Browse");
	private final JButton parseButton = new JButton("Start");
	private final JTextField playerField = new JTextField();
	private final JComboBox gameCombo = new JComboBox();
	private final JTextArea resultArea = new JTextArea(10, 10);
	
	private HP hp;
	private History his;
	private Thread ft;
	
	public HistoryPanel() {
		// [hispath l] 
		// [browse b] [follow tb]
		// [player f] [games cb]
		// [stats ta]
		
		pathLabel.setText("/Users/alex/Library/Application Support/PokerStars/HandHistory/tawvx");
		parseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parse();
			}
		});
		playerField.setColumns(20);
		playerField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				find();
			}
		});
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(pathLabel);
		add(progressLabel);
		JPanel filePanel = new JPanel();
		filePanel.add(browseButton);
		filePanel.add(parseButton);
		add(filePanel);
		JPanel playerPanel = new JPanel();
		playerPanel.add(playerField);
		playerPanel.add(gameCombo);
		add(playerPanel);
		JScrollPane areaScroller = new JScrollPane(resultArea);
		add(areaScroller);
	}
	
	private void parse() {
		if (ft == null) {
			hp = new PSHP();
			PSHP.out = Util.nullps;
			HEPoker.out = Util.nullps;
			his = new History();
			final long ct = System.currentTimeMillis();
			File dir = new File(pathLabel.getText());
			if (dir.isDirectory()) {
				FollowListener fl = new FollowListener() {
					@Override
					public void nextHand(Hand h) {
						his.addHand(h);
						if (h.date.getTime() > ct) {
							System.out.println(h);
							HandInfo.printhand2(h);
						}
					}
				};
				ft = new FollowThread(hp, dir, fl);
				ft.start();
			}
		}
	}
	
	void find() {
		if (his != null) {
			String name = playerField.getText();
			PlayerInfo pi = his.getInfo(name);
			if (pi != null) { 
				Set<String> games = pi.gmap.keySet();
				String[] gamesarr = games.toArray(new String[games.size()]);
				gameCombo.setModel(new DefaultComboBoxModel(gamesarr));
				System.out.println(pi);
				String s = pi.toLongString();
				resultArea.setText(s);
			} else {
				resultArea.setText("");
			}
		}
	}
	
}
