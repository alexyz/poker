package pet.ui.eq;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

class ButtonPanel extends JPanel {

	public ButtonPanel(final CalcPanel t) {
		// TODO ditch this, move to omahabuttonpanel
		final JSpinner oppSpin = new JSpinner(new SpinnerNumberModel(2, 1, 6, 1));
		JCheckBox hideBox = new JCheckBox("Hide Opp.");
		JButton clearBut = new JButton("Clear");
		JButton randBut = new JButton("Random");
		JButton calcBut = new JButton("Calculate");

		hideBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				t.hideOpp(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		randBut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int num = ((SpinnerNumberModel)oppSpin.getModel()).getNumber().intValue();
				t.random(num);
			}
		});

		clearBut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				t.clear();
			}
		});

		calcBut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				t.calc();
			}
		});

		add(oppSpin);
		add(hideBox);
		add(clearBut);
		add(randBut);
		add(calcBut);
	}
}