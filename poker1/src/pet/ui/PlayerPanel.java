package pet.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import pet.hp.util.PlayerInfo;

public class PlayerPanel extends JPanel {
	// [name]
	// [table-name,games,hands,value]
	// [pinfo]
	private final JTextField nameField = new JTextField();
	private final JTable playersTable = new JTable();
	private final JTextArea playerTextArea = new JTextArea();
	
	public PlayerPanel() {
		super(new BorderLayout());
		
		nameField.setColumns(10);
		nameField.setBorder(BorderFactory.createTitledBorder("Player Name"));
		nameField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				find();
			}
		});
		
		playersTable.setAutoCreateRowSorter(true);
		playersTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		playersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int r = playersTable.getSelectionModel().getMinSelectionIndex();
					if (r >= 0) {
						int sr = playersTable.convertRowIndexToModel(r);
						String player = (String) playersTable.getModel().getValueAt(sr, 0);
						System.out.println("selected " + r + " => " + sr + " => " + player);
						PlayerInfo pi = PokerFrame.getHistory().getPlayerInfo(player, false);
						playerTextArea.setText(pi.toLongString());
					}
				}
			}
		});
		
		JScrollPane playersTableScroller = new JScrollPane(playersTable);
		playersTableScroller.setBorder(BorderFactory.createTitledBorder("Players"));
		
		playerTextArea.setRows(5);
		playerTextArea.setLineWrap(true);
		
		JScrollPane playersTextAreaScroller = new JScrollPane(playerTextArea);
		playersTextAreaScroller.setBorder(BorderFactory.createTitledBorder("Player Info"));
		
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, playersTableScroller, playersTextAreaScroller);
		
		JPanel topPanel = new JPanel();
		topPanel.add(nameField);
				
		add(topPanel, BorderLayout.NORTH);
		add(split, BorderLayout.CENTER);
	}
	
	private void find() {
		String pattern = nameField.getText();
		playersTable.setModel(new PlayerTableModel(PokerFrame.getHistory().getPlayers(pattern)));
	}
}

class PlayerTableModel extends AbstractTableModel {
	private static final String[] cols = new String[] {
		"Player", "Games", "Hands" // value?
	};
	private static final Class<?>[] colcls = new Class<?>[] {
		String.class, Integer.class, Integer.class
	};
	private final List<String> players = new ArrayList<String>();
	private final Map<String,PlayerInfo> playerMap;
	public PlayerTableModel(Map<String, PlayerInfo> playerMap) {
		this.playerMap = playerMap;
		players.addAll(playerMap.keySet());
		Collections.sort(players);
	}
	@Override
	public int getColumnCount() {
		return cols.length;
	}
	
	@Override
	public String getColumnName(int c) {
		return cols[c];
	}

	@Override
	public int getRowCount() {
		return players.size();
	}
	
	@Override
	public Class<?> getColumnClass(int c) {
		return colcls[c];
	}

	@Override
	public Object getValueAt(int r, int c) {
		if (r < players.size()) {
			String player = players.get(r);
			PlayerInfo pi = playerMap.get(player);
			switch (c) {
			case 0: return pi.name;
			case 1: return pi.gmap.size(); // not sync
			case 2: return pi.hands;
			}
		}
		return null;
	}
	
}


