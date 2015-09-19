package pet.ui.hp;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import pet.PET;
import pet.hp.*;
import pet.hp.info.*;
import pet.ui.graph.GraphData;
import pet.ui.table.*;

public class GamesPanel extends JPanel implements HistoryListener {
	
	private final JComboBox<String> gameCombo = new JComboBox<>();
	private final MyJTable gamesTable = new MyJTable();
	private final JTextArea textArea = new JTextArea();
	private final JScrollPane textAreaScroller = new JScrollPane(textArea);
	private final JButton playerButton = new JButton("Player");
	private final JButton bankrollButton = new JButton("Bankroll");
	private final JButton handsButton = new JButton("Hands");
	private final JButton refreshButton = new JButton("Refresh");
	
	public GamesPanel() {
		super(new BorderLayout());
		
		gamesTable.setModel(new GameInfoTableModel(GameInfoTableModel.gameCols));
		gamesTable.setAutoCreateRowSorter(true);
		gamesTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		gamesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					PlayerGameInfo pgi = getSelectedPgi();
					if (pgi != null) {
						textArea.setText(pgi.toLongString());
						textArea.setCaretPosition(0);
						revalidate();
					} else {
						textArea.setText("");
					}
				}
			}
		});
		
		playerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PlayerGameInfo pgi = getSelectedPgi();
				if (pgi != null) {
					PET.getPokerFrame().displayPlayer(pgi.player.name);
				}
			}
		});
		
		bankrollButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PlayerGameInfo pgi = getSelectedPgi();
				if (pgi != null) {
					List<Hand> hands = PET.getHistory().getHands(pgi.player.name, pgi.game.id);
					String title = pgi.player.name + " - " + pgi.game.id;
					GraphData br = BankrollUtil.getBankRoll(pgi.player.name, hands, title);
					if (br != null) {
						PET.getPokerFrame().displayBankRoll(br);
					} else {
						System.out.println("no bank roll for " + pgi);
					}
				}
			}
		});
		
		handsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				PlayerGameInfo pgi = getSelectedPgi();
				if (pgi != null) {
				 	PET.getPokerFrame().displayHands(pgi.player.name, pgi.game.id);
				}
			}
		});
		
		// allow user to select game if there is only one in the list
		// and update if more hands have been added
		// though we could probably do both automatically
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateGame();
			}
		});
		
		// requires more than one in the list...
		gameCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					updateGame();
				}
			}
		});
		
		JPanel northPanel = new JPanel();
		northPanel.add(gameCombo);
		northPanel.add(refreshButton);
		add(northPanel, BorderLayout.NORTH);
		
		JScrollPane tableScroller = new JScrollPane(gamesTable);
		tableScroller.setBorder(BorderFactory.createTitledBorder("Player Game Infos"));
		
		textAreaScroller.setBorder(BorderFactory.createTitledBorder("Selected Player Game Info"));
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroller, textAreaScroller);
		splitPane.setResizeWeight(0.5);
		add(splitPane, BorderLayout.CENTER);
		
		JPanel southPanel = new JPanel();
		southPanel.add(playerButton);
		southPanel.add(bankrollButton);
		southPanel.add(handsButton);
		add(southPanel, BorderLayout.SOUTH);
	}
	
	private PlayerGameInfo getSelectedPgi() {
		int r = gamesTable.getSelectionModel().getMinSelectionIndex();
		if (r >= 0) {
			int sr = gamesTable.convertRowIndexToModel(r);
			GameInfoTableModel m = (GameInfoTableModel) gamesTable.getModel();
			PlayerGameInfo pgi = m.getRow(sr);
			System.out.println("selected " + r + " => " + sr + " => " + pgi);
			return pgi;
			
		} else {
			return null;
		}
	}
	
	private void updateGame() {
		System.out.println("update game");
		String selectedGameId = (String) gameCombo.getSelectedItem();
		Info info = PET.getPokerFrame().getInfo();
		List<PlayerGameInfo> gameInfos = info.getGameInfos(selectedGameId);
		GameInfoTableModel gamesModel = (GameInfoTableModel) gamesTable.getModel();
		gamesModel.setRows(gameInfos);
		gamesModel.setPopulation(info.getPopulation());
		repaint();
	}

	@Override
	public void handAdded(Hand hand) {
		// XXX could update table...
	}

	@Override
	public void gameAdded(Game game) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// update the game combo
				// XXX probably breaks current selection
				List<String> games = PET.getHistory().getGames();
				gameCombo.setModel(new DefaultComboBoxModel<>(games.toArray(new String[games.size()])));
			}
		});
	}
	
}
