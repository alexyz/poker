package pet.ui.eq;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.*;

import pet.eq.*;

/**
 * display the equity of the high or low value of a hand
 */
class EquityPanel extends JPanel {
	
	private final Font font = new Font("SansSerif", Font.PLAIN, 12);
	private final Font boldfont = new Font("SansSerif", Font.BOLD, 12);
	private final JLabel typeLab = new JLabel();
	private final JLabel equityLab = new JLabel();
	private final JLabel valueLab = new JLabel();
	
	public EquityPanel() {
		setLayout(new GridLayout(1, 3));
		
		typeLab.setFont(boldfont);
		
		equityLab.setFont(boldfont);
		equityLab.setVerticalAlignment(SwingConstants.CENTER);
		
		add(typeLab);
		add(equityLab);
		add(valueLab);
		
	}
	
	public void clearEquity() {
		typeLab.setText("");
		equityLab.setText("");
		valueLab.setText("");
		setToolTipText(null);
	}
	
	public void setEquity(Equity e) {
		typeLab.setFont(font);
		typeLab.setText(e.type.desc);
		
		equityLab.setFont(font);
		String et = String.format("Win: %.1f%%", e.won);
		if (e.tied > 1) {
			et += String.format(" (T: %.1f%%)", e.tied);
		}
		equityLab.setText(et);
		
		valueLab.setFont(font);
		valueLab.setText(e.current > 0 ? Poker.valueString(e.current) : "");
		
		revalidate();
	}
	
}
