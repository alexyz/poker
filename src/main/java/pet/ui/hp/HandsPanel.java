package pet.ui.hp;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import pet.PET;
import pet.hp.info.*;
import pet.ui.table.*;

/**
 * displays a list of hands for a particular game
 */
public class HandsPanel extends JPanel {
	
	private final JTextField playerField = new JTextField();
	private final JComboBox<String> gameCombo = new JComboBox<>();
	private final MyJTable handTable = new MyJTable();
	private final JTextArea textArea = new JTextArea();
	private final JComboBox<String> dateCombo = new JComboBox<>();
	private final JButton replayButton = new JButton("Replay");
	private final JButton lastHandButton = new JButton("Last Hand");
	private final JButton hudButton = new JButton("HUD");
	private List<HandInfo> handInfos;

	public HandsPanel() {
		super(new BorderLayout());
		playerField.setColumns(10);
		playerField.setBorder(BorderFactory.createTitledBorder("Player Name"));
		playerField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateGame(null);
			}
		});

		gameCombo.setBorder(BorderFactory.createTitledBorder("Game"));
		gameCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					updateDate();
				}
			}
		});
		
		dateCombo.setBorder(BorderFactory.createTitledBorder("Date"));
		dateCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					updateTable();
				}
			}
		});

		replayButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HandInfo hi = getHandInfo();
				if (hi != null) {
					PET.getPokerFrame().replayHand(hi.hand);
				}
			}
		});
		
		lastHandButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HandInfo hi = getHandInfo();
				if (hi != null) {
					PET.getPokerFrame().displayHand(hi.hand);
				}
			}
		});
		
		hudButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HandInfo hi = getHandInfo();
				if (hi != null) {
					PET.getPokerFrame().getHudManager().showHand(hi.hand);
				}
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
						PET.getPokerFrame().replayHand(hi.hand);
					}
					System.out.println("double click");
				}
			}
		});

		JPanel topPanel = new JPanel();
		topPanel.add(playerField);
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
		bottomPanel.add(lastHandButton);
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

	/**
	 * display hands for tournament
	 * TODO display the tournament id in the filters
	 */
	public void displayHands(long tournid) {
		playerField.setText("");
		gameCombo.setModel(new DefaultComboBoxModel<String>());
		List<HandInfo> hands = HandInfo.getHandInfos(PET.getHistory().getHands(tournid));
		((HandInfoTableModel)handTable.getModel()).setRows(hands);
		repaint();
	}
	
	public void displayHands(String player, String gameid) {
		playerField.setText(player);
		updateGame(gameid);
	}
	
	/**
	 * update game combo for player
	 */
	private void updateGame(String selectGameid) {
		String player = playerField.getText();
		System.out.println("update player " + player + " game " + selectGameid);
		
		PlayerInfo pi = PET.getPokerFrame().getInfo().getPlayerInfo(player);
		if (pi != null) {
			Vector<String> games = new Vector<>(pi.getGames().keySet());
			gameCombo.setModel(new DefaultComboBoxModel<>(games));
			if (selectGameid != null) {
				gameCombo.setSelectedItem(selectGameid);
			}
		} else {
			gameCombo.setModel(new DefaultComboBoxModel<String>());
		}
		
		updateDate();
	}
	
	/**
	 * update date combo for player and game
	 */
	private void updateDate() {
		System.out.println("update game");
		String player = playerField.getText();
		String gameId = (String) gameCombo.getSelectedItem();
		handInfos = HandInfo.getHandInfos(PET.getHistory().getHands(player, gameId));
		
		// build date lookup
		Map<String,Date> dateMap = new TreeMap<>();
		for (HandInfo hi : handInfos) {
			String datestr = DateFormat.getDateInstance().format(new Date(hi.hand.date));
			if (!dateMap.containsKey(datestr)) {
				dateMap.put(datestr, new Date(hi.hand.date));
			}
		}
		List<Date> dateList = new ArrayList<>(dateMap.values());
		Collections.sort(dateList);
		Vector<String> dates = new Vector<>();
		dates.add("");
		for (Date date : dateList) {
			dates.add(DateFormat.getDateInstance().format(date));
		}
		dateCombo.setModel(new DefaultComboBoxModel<>(dates));
		
		updateTable();
	}
	
	/**
	 * display hand infos for selected date
	 */
	private void updateTable() {
		System.out.println("update table");
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
			dateHandInfos = new ArrayList<>();
			for (HandInfo hi : handInfos) {
				Date hdate = new Date(hi.hand.date);
				if (hdate.after(date) && hdate.before(date2)) {
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


