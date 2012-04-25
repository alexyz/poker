package pet.ui;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.*;

import pet.hp.Game;
import pet.hp.info.History;
import pet.hp.info.PlayerGameInfo;
import pet.ui.ta.*;

public class TournPanel extends JPanel {
	private final JButton refreshButton = new JButton("Refresh");
	private final JComboBox gameCombo = new JComboBox();
	private final MyJTable tournTable = new MyJTable();
	// TODO hands button, game type combo
	
	public TournPanel() {
		super(new BorderLayout());
		
		// TODO tourn model
		//tournTable.setModel(new GameInfoTableModel(GameInfoTableModel.gameCols));
		tournTable.setAutoCreateRowSorter(true);
		tournTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO get list of tourns
			}
		});
		
		JPanel topPanel = new JPanel();
		topPanel.add(refreshButton);
		topPanel.add(gameCombo);
		add(topPanel, BorderLayout.NORTH);
		
		JScrollPane tableScroller = new JScrollPane(tournTable);
		tableScroller.setBorder(BorderFactory.createTitledBorder("Tournaments"));
		
		add(tableScroller, BorderLayout.CENTER);
	}
	
	
}
