package pet.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.*;
import java.text.DateFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import pet.eq.Poker;
import pet.hp.Hand;
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
						textArea.setText(hi.toString());
					}
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
			HandInfo hi = ((HandTableModel)handTable.getModel()).getRow(sr);
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
		handTable.setModel(new HandTableModel(hi));
	}
}

class HandTableModel extends AbstractTableModel {
	private static final String[] cols = new String[] {
		"Date", "MyHole", "Board", "Seats",
		"Winner", "WonOn", "Value"
		//, "pip", "af", "av"
	};
	private static final Class<?>[] colcls = new Class<?>[] {
		Date.class, String[].class, String[].class, Integer.class, String.class, String.class, Integer.class
	};
	private final List<HandInfo> hands;
	public HandTableModel(List<HandInfo> hands) {
		this.hands = hands;
	}
	public HandInfo getRow(int r) {
		return hands.get(r);
	}
	@Override
	public int getColumnCount() {
		return cols.length;
	}
	@Override
	public Class<?> getColumnClass(int c) {
		return colcls[c];
	}
	@Override
	public String getColumnName(int c) {
		return cols[c];
	}
	@Override
	public int getRowCount() {
		return hands.size();
	}
	@Override
	public Object getValueAt(int r, int c) {
		if (r < hands.size()) {
			HandInfo hi = hands.get(r);
			Hand h = hi.hand;
			switch (c) {
			case 0: return h.date;
			case 1: return h.myseat.hand;
			case 2: return h.board;
			case 3: return h.seats.length;
			}
		}
		return null;
	}

}

class DateRenderer extends DefaultTableCellRenderer {
	@Override
	protected void setValue(Object value) {
		if (value instanceof Date) {
			value = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(value);
		}
		super.setValue(value);
	}
}

class HandRenderer extends DefaultTableCellRenderer {
	public HandRenderer() {
		Font f = getFont();
		setFont(new Font("Monospaced", 0, f.getSize()));
	}
	@Override
	protected void setValue(Object value) {
		if (value instanceof String[]) {
			value = Poker.getCardString((String[]) value, true);
		}
		super.setValue(value);
	}
}


