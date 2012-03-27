package pet.ui;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import pet.hp.util.*;

public class SessionPanel extends JPanel {
	// name
	// game
	// from
	// to
	// table
	// handinfo
	private final JTextField nameField = new JTextField();
	private final JComboBox gameCombo = new JComboBox();
	private final JTextField fromField = new JTextField();
	private final JTextField toField = new JTextField();
	private final JTable handTable = new JTable();
	private final JTextArea textArea = new JTextArea();
	private final JButton sendButton = new JButton("Send");

	public SessionPanel() {
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

		fromField.setColumns(10);
		fromField.setBorder(BorderFactory.createTitledBorder("From Date"));

		toField.setColumns(10);
		toField.setBorder(BorderFactory.createTitledBorder("To Date"));

		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				PokerFrame.replay(getHandInfo().hand);
			}
		});

		handTable.setDefaultRenderer(Date.class, new DateRenderer());
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
						HandInfo hi = ((SessionTableModel)handTable.getModel()).getRow(sr);
						PokerFrame.replay(hi.hand);
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
		topPanel.add(fromField);
		topPanel.add(toField);
		topPanel.add(sendButton);

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroller, textScroller);
		add(topPanel, BorderLayout.NORTH);
		add(split, BorderLayout.CENTER);
	}

	private HandInfo getHandInfo() {
		int r = handTable.getSelectionModel().getMinSelectionIndex();
		if (r >= 0) {
			int sr = handTable.convertRowIndexToModel(r);
			HandInfo hi = ((SessionTableModel)handTable.getModel()).getRow(sr);
			System.out.println("selected " + r + " => " + sr + " => " + hi);
			return hi;
		}
		return null;
	}

	private void updateName(String name) {
		PlayerInfo pi = PokerFrame.getHistory().getPlayerInfo(name, false);
		Vector<String> games = new Vector<String>();
		games.add("");
		if (pi != null) {
			games.addAll(pi.gmap.keySet());
		}
		gameCombo.setModel(new DefaultComboBoxModel(games));
		handTable.setModel(new DefaultTableModel());
	}

	private void updateGame() {
		String player = nameField.getText();
		String game = (String) gameCombo.getSelectedItem();
		List<HandInfo> hi = PokerFrame.getHistory().getHands(player, game);
		handTable.setModel(new SessionTableModel(hi));
	}
}


