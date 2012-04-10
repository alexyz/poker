package pet.ui;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.text.DateFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import pet.hp.Hand;
import pet.hp.info.*;
import pet.ui.ta.*;

/**
 * todo send to eq panel button
 * send to rep button
 */
public class HandsPanel extends JPanel {
	// name
	// game
	// from
	// to
	// table
	// handinfo
	private final JTextField nameField = new JTextField();
	private final JComboBox gameCombo = new JComboBox();
	private final MyJTable<HandInfo> handTable = new MyJTable<HandInfo>(new HandInfoTableModel());
	private final JTextArea textArea = new JTextArea();
	private final JComboBox dateCombo = new JComboBox();
	private final JButton replayButton = new JButton("Replay");

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

		replayButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				PokerFrame.getInstance().displayHand(getHandInfo().hand);
			}
		});

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
						HandInfo hi = handTable.getModel().getRow(sr);
						PokerFrame.getInstance().displayHand(hi.hand);
					}
					System.out.println("double click");
				}
			}
		});

		JScrollPane tableScroller = new JScrollPane(handTable);
		tableScroller.setBorder(BorderFactory.createTitledBorder("Hands"));

		textArea.setBorder(BorderFactory.createTitledBorder("Hand Info"));
		JScrollPane textScroller = new JScrollPane(textArea);

		JPanel topPanel = new JPanel();
		topPanel.add(nameField);
		topPanel.add(gameCombo);
		topPanel.add(dateCombo);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(replayButton);

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroller, textScroller);
		add(topPanel, BorderLayout.NORTH);
		add(split, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
	}

	private HandInfo getHandInfo() {
		int r = handTable.getSelectionModel().getMinSelectionIndex();
		if (r >= 0) {
			int sr = handTable.convertRowIndexToModel(r);
			HandInfo hi = handTable.getModel().getRow(sr);
			System.out.println("selected " + r + " => " + sr + " => " + hi);
			return hi;
		}
		return null;
	}
	
	public void displayHands(String name, String game) {
		nameField.setText(name);
		updateName(game);
	}
	
	private void updateName(String game) {
		String name = nameField.getText();
		System.out.println("update name " + name + " game " + game);
		PlayerInfo pi = PokerFrame.getInstance().getHistory().getPlayerInfo(name);
		if (pi == null) {
			gameCombo.setModel(new DefaultComboBoxModel());
			dateCombo.setModel(new DefaultComboBoxModel());
			handTable.getModel().setRows(Collections.<HandInfo>emptyList());
			return;
		}
		
		Vector<String> games = new Vector<String>(pi.games.keySet());
		gameCombo.setModel(new DefaultComboBoxModel(games));
		if (game != null) {
			gameCombo.setSelectedItem(game);
		}
		
		updateGame();
	}

	private void updateGame() {
		System.out.println("update game");
		String player = nameField.getText();
		String game = (String) gameCombo.getSelectedItem();
		PokerFrame pf = PokerFrame.getInstance();
		History his = pf.getHistory();
		List<Hand> hands = his.getHands(player, game);
		
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
		
		List<HandInfo> handInfos = HandInfo.getHandInfos(hands);
		handTable.getModel().setRows(handInfos);
		repaint();
	}
}


