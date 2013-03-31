package pet.ui.hp;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;

import pet.PET;
import pet.hp.info.TournInfo;
import pet.ui.table.*;

/**
 * Shows all the tournaments
 */
public class TournPanel extends JPanel {
	
	private final JButton refreshButton = new JButton("Refresh");
	private final MyJTable tournTable = new MyJTable();
	private final JButton handsButton = new JButton("Hands");
	
	public TournPanel() {
		super(new BorderLayout());
		
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		
		handsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int r = tournTable.getSelectionModel().getMinSelectionIndex();
				if (r >= 0) {
					int sr = tournTable.convertRowIndexToModel(r);
					TournInfoTableModel m = (TournInfoTableModel) tournTable.getModel();
					TournInfo ti = m.getRow(sr);
					PET.getPokerFrame().displayHands(ti.tourn.id);
				}
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
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(handsButton);
		add(bottomPanel, BorderLayout.SOUTH);
	}
	
	public void refresh() {
		List<TournInfo> tis = PET.getPokerFrame().getInfo().getTournInfos();
		((TournInfoTableModel)tournTable.getModel()).setRows(tis);
	}
	
}
