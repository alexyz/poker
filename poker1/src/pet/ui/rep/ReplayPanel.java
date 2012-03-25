package pet.ui.rep;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import pet.hp.*;

/**
 * allows user to replay hands
 */
public class ReplayPanel extends JPanel {

	TableComponent table = new TableComponent();
	JComboBox stateCombo = new JComboBox();
	JButton prevButton = new JButton("<");
	JButton nextButton = new JButton(">");

	public ReplayPanel() {
		super(new BorderLayout());
		table.setBorder(BorderFactory.createTitledBorder("Hand Replay"));

		prevButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectState(-1);
			}
		});

		stateCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					table.setState((HandState) e.getItem());
				}
			}
		});

		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectState(1);
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(prevButton);
		buttonPanel.add(stateCombo);
		buttonPanel.add(nextButton);

		add(table, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.NORTH);
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

	/**
	 * display the given hand
	 */
	public void setHand(Hand h) {
		List<HandState> states = HandStateUtil.getStates(h);
		stateCombo.setModel(new DefaultComboBoxModel(states.toArray(new Object[states.size()])));
		stateCombo.setSelectedIndex(0);
		table.setState(states.get(0));
		repaint();
	}
}



