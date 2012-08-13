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

	public void setEquity(MEquity me, Equity e) {
		if (e.curwin || e.curtie) {
			setBorder(new LineBorder(Color.green));
		} else {
			setBorder(null);
		}
		
		typeLab.setText(Equity.getEqTypeName(e.eqtype));
		String s = String.format("Win: %.1f%%", e.won);
		if (e.tied != 0) {
			s += String.format("  Tie: %.1f%%", e.tied);
		}
		if (!me.exact) {
			s += " (est)";
		}
		equityLab.setText(s);
		if (e.current == 0) {
			valueLab.setText("");
		} else {
			valueLab.setFont(e.curwin ? boldfont : font);
			valueLab.setText(Poker.valueString(e.current));
		}
		
		revalidate();
	}
	
}
