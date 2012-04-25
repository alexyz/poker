package pet.ui;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import pet.hp.Hand;
import pet.hp.info.*;
import pet.ui.ta.*;

/**
 * displays a list of hands for a particular game
 */
public class HandsPanel extends JPanel {
	private final JTextField nameField = new JTextField();
	private final JComboBox gameCombo = new JComboBox();
	private final MyJTable handTable = new MyJTable();
	private final JTextArea textArea = new JTextArea();
	private final JComboBox dateCombo = new JComboBox();
	private final JButton replayButton = new JButton("Replay");
	private final JButton hudButton = new JButton("HUD");
	private List<HandInfo> handInfos;

	public HandsPanel() {
		super(new BorderLayout());
		nameField.setColumns(10);
		nameField.setBorder(BorderFactory.createTitledBorder("Player Name"));
		nameField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateName(null);
			}
		});

		gameCombo.setBorder(BorderFactory.createTitledBorder("Game"));
		gameCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					updateGame();
				}
			}
		});
		
		dateCombo.setBorder(BorderFactory.createTitledBorder("Date"));
		dateCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					updateDate();
				}
			}
		});

		replayButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				PokerFrame.getInstance().replayHand(getHandInfo().hand);
			}
		});
		
		hudButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				PokerFrame.getInstance().hudHand(getHandInfo().hand);
			}
		});

		handTable.setModel(new HandInfoTableModel());
		handTable.setDefaultRenderer(Date.class, new MyDateRenderer());
		handTable.setDefaultRenderer(String[].class, new HandRenderer());
		handTable.setAutoCreateRowSorter(true);
		handTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		handTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					HandInfo hi = getHandInfo();
					if (hi != null) {
						textArea.setText(hi.getDescription());
					}
				}
			}
		});
		handTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					int r = handTable.rowAtPoint(e.getPoint());
					if (r >= 0) {
						int sr = handTable.convertRowIndexToModel(r);
						HandInfo hi = ((HandInfoTableModel)handTable.getModel()).getRow(sr);
						PokerFrame.getInstance().replayHand(hi.hand);
					}
					System.out.println("double click");
				}
			}
		});

		JPanel topPanel = new JPanel();
		topPanel.add(nameField);
		topPanel.add(gameCombo);
		topPanel.add(dateCombo);
		
		JScrollPane tableScroller = new JScrollPane(handTable);
		tableScroller.setBorder(BorderFactory.createTitledBorder("Hand Infos"));
		JScrollPane textScroller = new JScrollPane(textArea);
		textScroller.setBorder(BorderFactory.createTitledBorder("Selected Hand Info"));
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroller, textScroller);
		split.setResizeWeight(0.5);

		JPanel bottomPanel = new JPanel();
		bottomPanel.add(replayButton);
		bottomPanel.add(hudButton);
		
		add(topPanel, BorderLayout.NORTH);
		add(split, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
	}

	private HandInfo getHandInfo() {
		int r = handTable.getSelectionModel().getMinSelectionIndex();
		if (r >= 0) {
			int sr = handTable.convertRowIndexToModel(r);
			HandInfo hi = ((HandInfoTableModel)handTable.getModel()).getRow(sr);
			System.out.println("selected " + r + " => " + sr + " => " + hi);
			return hi;
		}
		return null;
	}
	
	public void displayHands(String name, String game) {
		nameField.setText(name);
		updateName(game);
	}
	
	private void clear() {
		gameCombo.setModel(new DefaultComboBoxModel());
		dateCombo.setModel(new DefaultComboBoxModel());
		((HandInfoTableModel)handTable.getModel()).setRows(Collections.<HandInfo>emptyList());
	}
	
	private void updateName(String selectGame) {
		String name = nameField.getText();
		System.out.println("update name " + name + " game " + selectGame);
		PlayerInfo pi = PokerFrame.getInstance().getHistory().getPlayerInfo(name);
		clear();
		if (pi != null) {
			Vector<String> games = new Vector<String>(pi.games.keySet());
			gameCombo.setModel(new DefaultComboBoxModel(games));
			if (selectGame != null) {
				gameCombo.setSelectedItem(selectGame);
			}
		}
		// update table
		updateGame();
	}
	
	private void updateGame() {
		System.out.println("update game");
		String player = nameField.getText();
		String gameId = (String) gameCombo.getSelectedItem();
		History history = PokerFrame.getInstance().getHistory();
		List<Hand> hands = history.getHands(player, gameId);
		handInfos = HandInfo.getHandInfos(hands);
		
		// build date lookup
		Map<String,Date> dateMap = new TreeMap<String,Date>();
		for (Hand hand : hands) {
			String datestr = DateFormat.getDateInstance().format(hand.date);
			if (!dateMap.containsKey(datestr)) {
				dateMap.put(datestr, hand.date);
			}
		}
		List<Date> dateList = new ArrayList<Date>(dateMap.values());
		Collections.sort(dateList);
		Vector<String> dates = new Vector<String>();
		dates.add("");
		for (Date date : dateList) {
			dates.add(DateFormat.getDateInstance().format(date));
		}
		dateCombo.setModel(new DefaultComboBoxModel(dates));
		
		updateDate();
	}
	
	private void updateDate() {
		System.out.println("update date");
		String dateStr = (String) dateCombo.getSelectedItem();
		
		// get hand infos for date
		List<HandInfo> dateHandInfos;
		if (dateStr != null && dateStr.length() > 0) {
			Date date;
			try {
				date = DateFormat.getDateInstance().parse(dateStr);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			Date date2 = new Date(date.getTime() + (24 * 60 * 60 * 1000L));
			dateHandInfos = new ArrayList<HandInfo>();
			for (HandInfo hi : handInfos) {
				if (hi.hand.date.after(date) && hi.hand.date.before(date2)) {
					dateHandInfos.add(hi);
				}
			}
			
		} else {
			dateHandInfos = handInfos;
		}
		
		((HandInfoTableModel)handTable.getModel()).setRows(dateHandInfos);
		repaint();
	}
}


