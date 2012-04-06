package pet.ui;

import java.awt.BorderLayout;
import java.awt.event.*;
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
	private final JButton replayButton = new JButton("Replay");

	public HandsPanel() {
		super(new BorderLayout());
		nameField.setColumns(10);
		nameField.setBorder(BorderFactory.createTitledBorder("Player Name"));
		nameField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateName(nameField.getText());
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

	private void updateName(String name) {
		PlayerInfo pi = PokerFrame.getInstance().getHistory().getPlayerInfo(name);
		Vector<String> games = new Vector<String>();
		games.add("");
		if (pi != null) {
			games.addAll(pi.games.keySet());
		}
		gameCombo.setModel(new DefaultComboBoxModel(games));
		handTable.getModel().setRows(Collections.<HandInfo>emptyList());
	}

	private void updateGame() {
		String player = nameField.getText();
		String game = (String) gameCombo.getSelectedItem();
		PokerFrame pf = PokerFrame.getInstance();
		History his = pf.getHistory();
		List<Hand> hands = his.getHands(player, game);
		List<HandInfo> handInfos = HandInfo.getHandInfos(hands);
		handTable.getModel().setRows(handInfos);
		repaint();
	}
}


