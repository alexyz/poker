package pet.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import pet.hp.Hand;
import pet.hp.state.HandState;
import pet.hp.info.FollowListener;
import pet.ui.ta.HandStateTableModel;
import pet.ui.ta.MyJTable;

public class HUDPanel extends JPanel implements FollowListener {
	public HUDPanel() {
		super(new BorderLayout());
		JScrollPane p = new JScrollPane();
		MyJTable<HandState> t = new MyJTable<HandState>(new HandStateTableModel());
		// index for list of list of handstates
	}

	@Override
	public void nextHand(Hand h) {
		// create handstates, add to list
		// display most recent in hud
	}
}
