package pet.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import pet.hp.Hand;
import pet.hp.History;
import pet.hp.info.*;
import pet.ui.gr.GraphData;
import pet.ui.ta.*;

/**
 * player info and player game info panel
 * 
 * ( ) Self ( ) [Name] [Search]
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
	
	private final JRadioButton selfButton = new JRadioButton("Self");
	private final JRadioButton nameButton = new JRadioButton("Name:");
	private final JButton searchButton = new JButton("Search");
	
	public PlayerPanel() {
		super(new BorderLayout());
		
		nameField.setColumns(10);
		//nameField.setBorder(BorderFactory.createTitledBorder("Player Name"));
		nameField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				find();
			}
		});
		
		
		ButtonGroup bg = new ButtonGroup();
		selfButton.getModel().setGroup(bg);
		nameButton.getModel().setGroup(bg);
		
		selfButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					nameField.setEnabled(false);
				}
			}
		});
		
		nameButton.setSelected(true);
		nameButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					nameField.setEnabled(true);
				}
			}
		});
		
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
						gamesModel.setRows(pi.getGames().values());
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
					List<Hand> hands = pf.getHistory().getHands(gi.player.name, gi.game.id);
					GraphData bankRoll = BankrollUtil.getBankRoll(hands, gi.player.name, gi.game.id);
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
		gamesTableScroller.setBorder(BorderFactory.createTitledBorder("Player Game Infos"));
		
		JScrollPane gameTextAreaScroller = new JScrollPane(gameTextArea);
		gameTextAreaScroller.setBorder(BorderFactory.createTitledBorder("Selected Player Game Info"));
		
		JScrollPane playersTableScroller = new JScrollPane(playersTable);
		playersTableScroller.setBorder(BorderFactory.createTitledBorder("Player Infos"));
		
		JPanel mainPanel = new JPanel(new GridLayout(3, 1));
		mainPanel.add(playersTableScroller);
		mainPanel.add(gamesTableScroller);
		mainPanel.add(gameTextAreaScroller);
		
		JPanel topPanel = new JPanel();
		topPanel.add(selfButton);
		topPanel.add(nameButton);
		topPanel.add(nameField);
		topPanel.add(searchButton);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(handsButton);
		bottomPanel.add(bankrollButton);
				
		add(topPanel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
	}
	
	/** search for player and update table */
	private void find() {
		PokerFrame pf = PokerFrame.getInstance();
		Info info = pf.getInfo();
		
		PlayerInfoTableModel playersModel = (PlayerInfoTableModel) playersTable.getModel();
		if (nameButton.isSelected()) {
			String pattern = nameField.getText();
			playersModel.setRows(info.getPlayers(pattern));
		} else {
			History history = pf.getHistory();
			playersModel.setRows(info.getPlayers(history.getSelf()));
		}
		
		GameInfoTableModel gamesModel = (GameInfoTableModel) gamesTable.getModel();
		gamesModel.setRows(Collections.<PlayerGameInfo>emptyList());
		gamesModel.setPopulation(info.getPopulation());
		
		System.out.println("players table now has " + playersTable.getRowCount() + " rows");
		repaint();
	}

	/** search for the given player */
	public void displayPlayer(String player) {
		nameField.setText(player);
		find();
	}
}


