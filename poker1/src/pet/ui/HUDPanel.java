package pet.ui;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.text.DateFormat;
import java.util.*;

import javax.swing.*;

import pet.hp.Hand;
import pet.hp.state.HandState;
import pet.hp.state.HandStateUtil;
import pet.hp.info.FollowListener;
import pet.ui.ta.HandStateTableModel;
import pet.ui.ta.MyJTable;

public class HUDPanel extends JPanel implements FollowListener {
	private final JComboBox stateCombo = new JComboBox(new DefaultComboBoxModel());
	private final MyJTable<HandState> handTable = new MyJTable<HandState>(new HandStateTableModel());
	private final JButton prevButton = new JButton(PokerFrame.LEFT_TRI);
	private final JButton nextButton = new JButton(PokerFrame.RIGHT_TRI);
	private final JButton replayButton = new JButton("Replay");
	private final Date startDate = new Date();
	
	public HUDPanel() {
		super(new BorderLayout());

		stateCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				HandStates states = (HandStates) e.getItem();
				handTable.getModel().setRows(states.states);
			}
		});
		
		prevButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectState(-1);
			}
		});

		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectState(1);
			}
		});
		
		replayButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HandStates hs = (HandStates) stateCombo.getSelectedItem();
				if (hs != null) {
					PokerFrame.getInstance().replayHand(hs.hand);
				}
			}
		});

		JPanel topPanel = new JPanel();
		topPanel.add(prevButton);
		topPanel.add(stateCombo);
		topPanel.add(nextButton);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(replayButton);
		
		JScrollPane tableScroller = new JScrollPane(handTable);
		add(topPanel, BorderLayout.NORTH);
		add(tableScroller, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
	}
	

	private void selectState(int off) {
		int i = stateCombo.getSelectedIndex();
		if (i >= 0) {
			i += off;
			if (i >= 0 && i < stateCombo.getItemCount()) {
				System.out.println("setting index " + i);
				stateCombo.setSelectedIndex(i);
				repaint();
			}
		}
	}

	@Override
	public void nextHand(Hand hand) {
		nextHand(hand, false);
	}
	
	public void nextHand(Hand hand, boolean force) {
		// create hand states, add to list
		// display most recent in hud
		if (force || hand.date.after(startDate)) {
			// FIXME don't add if force and already present
			((DefaultComboBoxModel)stateCombo.getModel()).addElement(new HandStates(hand));
			stateCombo.setSelectedIndex(stateCombo.getModel().getSize() - 1);
		}
	}
	
	@Override
	public void doneFile(int done, int total) {
		//
	}
}

/** represents a list of hand states for a hand */
class HandStates {
	public final List<HandState> states;
	public final Hand hand;
	public HandStates(Hand hand) {
		this.hand = hand;
		this.states = HandStateUtil.getStates(hand);
	}
	@Override
	public String toString() {
		return hand.tablename + " " + DateFormat.getDateTimeInstance().format(hand.date);
	}
}
