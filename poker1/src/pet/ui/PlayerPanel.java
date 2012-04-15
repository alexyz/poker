package pet.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.Collections;

import javax.swing.*;
import javax.swing.event.*;

import pet.hp.info.*;
import pet.ui.gr.GraphData;
import pet.ui.ta.*;

/**
 * TODO send to bankroll button
 * update table model
 * add all time win/loss column
 */
public class PlayerPanel extends JPanel {
	// [name]
	// [table-name,games,hands,value]
	// [pinfo]
	private final JTextField nameField = new JTextField();
	private final MyJTable playersTable = new MyJTable();
	private final MyJTable gamesTable = new MyJTable();
	private final JTextArea gameTextArea = new JTextArea();
	private final JButton bankrollButton = new JButton("Bankroll");
	private final JButton handsButton = new JButton("Hands");
	
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
		
		playersTable.setModel(new PlayerInfoTableModel());
		playersTable.setAutoCreateRowSorter(true);
		playersTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		playersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int r = playersTable.getSelectionModel().getMinSelectionIndex();
					if (r >= 0) {
						int sr = playersTable.convertRowIndexToModel(r);
						PlayerInfoTableModel playersModel = (PlayerInfoTableModel) playersTable.getModel();
						PlayerInfo pi = playersModel.getRow(sr);
						System.out.println("selected " + r + " => " + sr + " => " + pi);
						GameInfoTableModel gamesModel = (GameInfoTableModel) gamesTable.getModel();
						gamesModel.setRows(pi.games.values());
						revalidate();
					}
				}
			}
		});
		
		gamesTable.setModel(new GameInfoTableModel(GameInfoTableModel.playerCols));
		gamesTable.setAutoCreateRowSorter(true);
		gamesTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		gamesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int r = gamesTable.getSelectionModel().getMinSelectionIndex();
					if (r >= 0) {
						int sr = gamesTable.convertRowIndexToModel(r);
						GameInfoTableModel gamesModel = (GameInfoTableModel) gamesTable.getModel();
						PlayerGameInfo gi = gamesModel.getRow(sr);
						System.out.println("selected " + r + " => " + sr + " => " + gi);
						gameTextArea.setText(gi.toLongString());
						revalidate();
					}
				}
			}
		});
		
		gameTextArea.setRows(1);
		gameTextArea.setLineWrap(true);
		
		bankrollButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO get list of hands from history
				int r = gamesTable.getSelectionModel().getMinSelectionIndex();
				if (r >= 0) {
					int sr = gamesTable.convertRowIndexToModel(r);
					GameInfoTableModel gamesModel = (GameInfoTableModel) gamesTable.getModel();
					PlayerGameInfo gi = gamesModel.getRow(sr);
					System.out.println("selected " + r + " => " + sr + " => " + gi);
					PokerFrame pf = PokerFrame.getInstance();
					GraphData bankRoll = pf.getHistory().getBankRoll(gi.player.name, gi.game.id);
					pf.displayBankRoll(bankRoll);
				}
			}
		});
		
		handsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int r = gamesTable.getSelectionModel().getMinSelectionIndex();
				if (r >= 0) {
					int sr = gamesTable.convertRowIndexToModel(r);
					GameInfoTableModel gamesModel = (GameInfoTableModel) gamesTable.getModel();
					PlayerGameInfo gi = gamesModel.getRow(sr);
					PokerFrame.getInstance().displayHands(gi.player.name, gi.game.id);
				}
			}
		});
		
		JScrollPane gamesTableScroller = new JScrollPane(gamesTable);
		gamesTableScroller.setBorder(BorderFactory.createTitledBorder("Player Games"));
		
		JScrollPane gameTextAreaScroller = new JScrollPane(gameTextArea);
		gameTextAreaScroller.setBorder(BorderFactory.createTitledBorder("Player Game Info"));
		
		JScrollPane playersTableScroller = new JScrollPane(playersTable);
		playersTableScroller.setBorder(BorderFactory.createTitledBorder("Players"));
		
		JPanel mainPanel = new JPanel(new GridLayout(3, 1));
		mainPanel.add(playersTableScroller);
		mainPanel.add(gamesTableScroller);
		mainPanel.add(gameTextAreaScroller);
		
		JPanel topPanel = new JPanel();
		topPanel.add(nameField);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(handsButton);
		bottomPanel.add(bankrollButton);
				
		add(topPanel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
	}
	
	private void find() {
		String pattern = nameField.getText();
		PokerFrame pf = PokerFrame.getInstance();
		History his = pf.getHistory();
		
		PlayerInfoTableModel playersModel = (PlayerInfoTableModel) playersTable.getModel();
		playersModel.setRows(his.getPlayers(pattern));
		
		GameInfoTableModel gamesModel = (GameInfoTableModel) gamesTable.getModel();
		gamesModel.setRows(Collections.<PlayerGameInfo>emptyList());
		gamesModel.setPopulation(his.getPopulation());
		
		System.out.println("players table now has " + playersTable.getRowCount() + " rows");
		repaint();
	}
}


