package pet.ui.eq;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;

import pet.eq.*;

/**
 * display the equity of the high or low value of a hand
 */
class EquityPanel extends JPanel {
	
	private final Font font = UIManager.getFont("Label.font");
	private final Font boldfont = font.deriveFont(Font.BOLD);
	private final JLabel typeLab = new JLabel();
	private final JLabel equityLab = new JLabel();
	private final JLabel tieLab = new JLabel();
	private final JLabel valueLab = new JLabel();
	
	public EquityPanel() {
		setLayout(new GridLayout(1, 4));
		
		typeLab.setFont(boldfont);
		
		equityLab.setFont(boldfont);
		equityLab.setVerticalAlignment(SwingConstants.CENTER);
		
		add(typeLab);
		add(equityLab);
		add(tieLab);
		add(valueLab);
		
	}
	
	public void clearEquity() {
		typeLab.setText("");
		equityLab.setText("");
		tieLab.setText("");
		valueLab.setText("");
		setToolTipText(null);
	}
	
	public void setEquity(MEquity me, Equity e) {
		Font f;
		if (e.curwin || e.curtie) {
			setBorder(new LineBorder(Color.green));
			f = boldfont;
		} else {
			setBorder(null);
			f = font;
		}
		
		typeLab.setFont(f);
		typeLab.setText(Equity.getEqTypeName(e.eqtype));
		
		equityLab.setFont(f);
		equityLab.setText(String.format("Win: %.1f%%", e.won));
		
		tieLab.setFont(f);
		tieLab.setText(e.tied != 0 ? String.format("Tie: %.1f%%", e.tied) : "");
		
		valueLab.setFont(f);
		valueLab.setText(e.current > 0 ? Poker.valueString(e.current) : "");
		
		revalidate();
	}
	
}
