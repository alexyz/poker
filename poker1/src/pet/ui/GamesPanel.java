package pet.ui;

import java.awt.BorderLayout;

import javax.swing.*;

import pet.ui.ta.MyJTable;

public class GamesPanel extends JPanel {
	private final JComboBox gameCombo = new JComboBox();
	private final MyJTable gamesTable = new MyJTable();
	private final JTextArea textArea = new JTextArea();
	public GamesPanel() {
		super(new BorderLayout());
		JPanel topPanel = new JPanel();
		topPanel.add(gameCombo);
		JScrollPane tableScroller = new JScrollPane(gamesTable);
		
	}
}
