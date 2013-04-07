package pet.ui.hp;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Collections;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import pet.PET;
import pet.hp.Hand;
import pet.hp.History;
import pet.hp.info.*;
import pet.ui.graph.GraphData;
import pet.ui.table.*;

/**
 * player info and player game info panel
 */
public class PlayerPanel extends JPanel {

	private final PlayerField playerField = new PlayerField();
	private final JButton searchButton = new JButton("Search");
	private final MyJTable playersTable = new MyJTable();
	private final MyJTable gamesTable = new MyJTable();
	private final JTextArea gameTextArea = new JTextArea();
	private final JButton bankrollButton = new JButton("Bankroll");
	private final JButton handsButton = new JButton("Hands");
	
	public PlayerPanel() {
		super(new BorderLayout());
		
		playerField.addPropertyChangeListener(PlayerField.FIND_PROP_CHANGE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				find();
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
				int r = gamesTable.getSelectionModel().getMinSelectionIndex();
				if (r >= 0) {
					int sr = gamesTable.convertRowIndexToModel(r);
					GameInfoTableModel gamesModel = (GameInfoTableModel) gamesTable.getModel();
					PlayerGameInfo pgi = gamesModel.getRow(sr);
					System.out.println("selected " + r + " => " + sr + " => " + pgi);
					List<Hand> hands = PET.getHistory().getHands(pgi.player.name, pgi.game.id);
					String title = pgi.player.name + " - " + pgi.game.id;
					GraphData br = BankrollUtil.getBankRoll(pgi.player.name, hands, title);
					if (br != null) {
						PET.getPokerFrame().displayBankRoll(br);
					} else {
						System.out.println("no bankroll for " + pgi);
					}
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
					PET.getPokerFrame().displayHands(gi.player.name, gi.game.id);
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
		topPanel.add(playerField);
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
		Info info = PET.getPokerFrame().getInfo();
		
		PlayerInfoTableModel playersModel = (PlayerInfoTableModel) playersTable.getModel();
		if (playerField.isSelfSelected()) {
			History history = PET.getHistory();
			playersModel.setRows(info.getPlayers(history.getSelf()));
		} else {
			String pattern = playerField.getPlayerName();
			playersModel.setRows(info.getPlayers(pattern));
		}
		
		GameInfoTableModel gamesModel = (GameInfoTableModel) gamesTable.getModel();
		gamesModel.setRows(Collections.<PlayerGameInfo>emptyList());
		gamesModel.setPopulation(info.getPopulation());
		
		System.out.println("players table now has " + playersTable.getRowCount() + " rows");
		repaint();
	}

	/** search for the given player */
	public void displayPlayer(String name) {
		playerField.setPlayerName(name);
		find();
	}
}


