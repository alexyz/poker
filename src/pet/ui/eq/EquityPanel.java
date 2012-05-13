package pet.ui.eq;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.*;

import javax.swing.*;

import pet.eq.*;

/**
 * display the equity of the high or low value of a hand
 */
class EquityPanel extends JPanel {

	// TODO move to pf
	private static final Font font = UIManager.getFont("Label.font");
	private static final Font boldfont = font.deriveFont(Font.BOLD);

	private final JLabel equityLab = new JLabel();
	private final JLabel valueLab = new JLabel();
	private final JLabel outsLab = new JLabel();

	public EquityPanel() {
		setLayout(new GridLayout(1, 3));

		equityLab.setFont(boldfont);
		equityLab.setVerticalAlignment(SwingConstants.CENTER);

		add(equityLab);
		add(valueLab);
		add(outsLab);

	}

	public void clearHandEquity() {
		equityLab.setText("");
		valueLab.setText("");
		outsLab.setText("");
		setToolTipText(null);
	}

	public void setHandEquity(MEquity me, Equity e) {
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

		if (e.current == 0 || e.curwin) {
			outsLab.setText("");
			setToolTipText(null);

		} else {
			List<String> minl = new ArrayList<String>(), majl = new ArrayList<String>();
			for (Map.Entry<String,Float> o : e.outs.entrySet()) {
				float v = o.getValue();
				if (v > 0) {
					if (v > 50) {
						majl.add(o.getKey());
					} else {
						minl.add(o.getKey());
					}
				}
			}
			Collections.sort(majl, Cmp.cardCmp);
			Collections.sort(minl, Cmp.cardCmp);
			outsLab.setText("Outs: " + majl.size() + " out of " + me.remCards);
			if (majl.size() > 0) {
				setToolTipText("<html><b>Major outs</b><br/>" + majl + "</html>");
			} else if (minl.size() > 0) {
				setToolTipText("<html><b>Minor outs</b><br/>" + minl + "</html>");
			}
		}
	}
	
}
