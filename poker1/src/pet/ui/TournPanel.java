package pet.ui;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;

import pet.hp.info.TournInfo;
import pet.ui.ta.*;

public class TournPanel extends JPanel {
	private final JButton refreshButton = new JButton("Refresh");
	private final MyJTable tournTable = new MyJTable();
	// TODO hands button, pgi table
	
	public TournPanel() {
		super(new BorderLayout());
		
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		
		tournTable.setModel(new TournInfoTableModel());
		tournTable.setAutoCreateRowSorter(true);
		tournTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JScrollPane tableScroller = new JScrollPane(tournTable);
		tableScroller.setBorder(BorderFactory.createTitledBorder("Tournaments"));

		JPanel topPanel = new JPanel();
		topPanel.add(refreshButton);
		add(topPanel, BorderLayout.NORTH);
		
		add(tableScroller, BorderLayout.CENTER);
	}
	
	public void refresh() {
		List<TournInfo> tis = PokerFrame.getInstance().getInfo().getTournInfos();
		((TournInfoTableModel)tournTable.getModel()).setRows(tis);
	}
	
}
