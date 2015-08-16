package pet.ui.hp;

import java.awt.FlowLayout;
import java.awt.event.*;

import javax.swing.*;

/**
 * Composite component to allow selection of self or the name of a player, i.e.
 * ( ) Self ( ) [Name]
 */
public class PlayerField extends JPanel {
	public static final String FIND_PROP_CHANGE = "findit";
	private final JRadioButton selfButton = new JRadioButton("Self");
	private final JRadioButton nameButton = new JRadioButton("Name:");
	private final JTextField nameField = new JTextField();
	
	public PlayerField() {
		super(new FlowLayout(FlowLayout.LEFT, 5, 0));
		ButtonGroup bg = new ButtonGroup();
		selfButton.getModel().setGroup(bg);
		nameButton.getModel().setGroup(bg);
		
		selfButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					nameField.setEnabled(false);
				}
			}
		});
		
		nameButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					nameField.setEnabled(true);
				}
			}
		});
		
		nameField.setColumns(10);
		nameField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				firePropertyChange(FIND_PROP_CHANGE, false, true);
			}
		});

		selfButton.setSelected(true);
		add(selfButton);
		add(nameButton);
		add(nameField);
	}
	
	public boolean isSelfSelected() {
		return selfButton.isSelected();
	}
	
	public String getPlayerName() {
		return nameField.getText();
	}
	
	public void setPlayerName(String name) {
		nameField.setText(name);
		nameButton.setSelected(true);
	}
	
}
