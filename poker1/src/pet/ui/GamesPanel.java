package pet.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.*;

import pet.hp.Game;
import pet.hp.info.History;
import pet.hp.info.PlayerGameInfo;
import pet.ui.ta.*;

public class GamesPanel extends JPanel {
	private final JComboBox gameCombo = new JComboBox();
	private final JButton refreshButton = new JButton("Refresh");
	private final MyJTable gamesTable = new MyJTable();
	private final JTextArea textArea = new JTextArea();
	
	public GamesPanel() {
		super(new BorderLayout());
		
		gamesTable.setModel(new GameInfoTableModel(GameInfoTableModel.gameCols));
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
						textArea.setText(gi.toLongString());
						revalidate();
					}
				}
			}
		});
		
		gameCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					updateGame();
				}
			}
		});
		
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initGames();
			}
		});
		
		JPanel topPanel = new JPanel();
		topPanel.add(refreshButton);
		topPanel.add(gameCombo);
		add(topPanel, BorderLayout.NORTH);
		
		JScrollPane tableScroller = new JScrollPane(gamesTable);
		tableScroller.setBorder(BorderFactory.createTitledBorder("Games"));
		
		JScrollPane textAreaScroller = new JScrollPane(textArea);
		textAreaScroller.setBorder(BorderFactory.createTitledBorder("Player Game Info"));
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroller, textAreaScroller);
		splitPane.setResizeWeight(0.5);
		add(splitPane, BorderLayout.CENTER);
	}
	
	private void initGames() {
		Map<String, Game> games = PokerFrame.getInstance().getHistory().getGames();
		String[] gameids = games.keySet().toArray(new String[games.size()]);
		gameCombo.setModel(new DefaultComboBoxModel(gameids));
	}
	
	private void updateGame() {
		System.out.println("update game");
		History history = PokerFrame.getInstance().getHistory();
		Object selected = gameCombo.getSelectedItem();
		Game game = history.getGames().get(selected);
		List<PlayerGameInfo> gameInfos = history.getGameInfos(game);
		GameInfoTableModel gamesModel = (GameInfoTableModel) gamesTable.getModel();
		gamesModel.setRows(gameInfos);
		gamesModel.setPopulation(history.getPopulation());
		repaint();
	}
	
}
