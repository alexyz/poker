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
	private final JLabel[] rankLabs;

	public EquityPanel(boolean high) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

		equityLab.setFont(boldfont);
		equityLab.setVerticalAlignment(SwingConstants.CENTER);

		// eq and val labels
		JPanel valcolsPan = new JPanel(new GridLayout(1, 3));
		valcolsPan.add(equityLab);
		valcolsPan.add(valueLab);
		valcolsPan.add(outsLab);

		add(valcolsPan);

		if (high) {
			// high value rank labels
			rankLabs = new JLabel[Poker.RANKS];
			JPanel rankcolsPan = new JPanel(new GridLayout(1, rankLabs.length));
			for (int n = 0; n < rankLabs.length; n++) {
				JLabel l = new JLabel();
				l.setVerticalAlignment(SwingConstants.CENTER);
				l.setPreferredSize(new Dimension(boldfont.getSize() * 4, boldfont.getSize() + 4));
				l.setMinimumSize(l.getPreferredSize());
				rankLabs[n] = l;
				rankcolsPan.add(rankLabs[n]);
			}
			add(rankcolsPan);
			
		} else {
			rankLabs = new JLabel[0];
		}

	}

	public void clearHandEquity() {
		equityLab.setText("");
		valueLab.setText("");
		outsLab.setText("");
		setToolTipText(null);
		for (JLabel rl : rankLabs) {
			rl.setFont(font);
			rl.setText("");
		}
	}

	public void setHandEquity(MEquity me, boolean high) {
		Equity e = high ? me.hi : me.lo;
		String s = String.format("Win: %.1f%%", e.won);
		if (e.tied != 0) {
			s += String.format("  Split: %.1f%%", e.tied);
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
			outsLab.setText("Outs: " + majl.size() + " out of " + me.rem);
			if (majl.size() > 0) {
				setToolTipText("<html><b>Major outs</b><br/>" + majl + "</html>");
			} else if (minl.size() > 0) {
				setToolTipText("<html><b>Minor outs</b><br/>" + minl + "</html>");
			}
		}

		for (int n = 0; n < rankLabs.length; n++) {
			JLabel rl = rankLabs[n];
			rl.setForeground(e.wonrank[n] > 0 ? Color.black : Color.darkGray);
			rl.setFont(e.wonrank[n] > 0 ? boldfont : font);
			rl.setText(String.format("%s: %.0f", Poker.ranknames[n], e.wonrank[n]));
		}

	}
	
}
