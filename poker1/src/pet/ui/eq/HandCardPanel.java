package pet.ui.eq;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import pet.eq.*;

/**
 * extends card panel to show hand equity statistics
 */
class HandCardPanel extends CardPanel {
	
	private static final Font font = UIManager.getFont("Label.font");
	private static final Font boldfont = font.deriveFont(Font.BOLD);
	
	/**
	 * get cards from array of hand card panels.
	 * return null if no cards set or some hands incomplete
	 */
	public static String[][] getCards(HandCardPanel[] cps) {
		List<String[]> hands = new ArrayList<String[]>();
		for (HandCardPanel cp : cps) {
			String[] cards = cp.getCards();
			if (cards.length > 0 && cards.length < cp.getMinCards()) {
				return null;
			}
			hands.add(cards);
		}
		System.out.println("hands: " + hands.size());
		return hands.size() == 0 ? null : hands.toArray(new String[hands.size()][]);
	}

	private final JLabel equityLab = new JLabel();
	private final JLabel valueLab = new JLabel();
	private final JLabel outsLab = new JLabel();
	private final JLabel[] rankLabs = new JLabel[9];

	public HandCardPanel(String name, List<CardLabel> cls, int mincards, int maxcards) {
		super(name, cls, mincards, maxcards);
		equityLab.setFont(boldfont);
		equityLab.setVerticalAlignment(SwingConstants.CENTER);
		
		// eq and val labels
		JPanel valcolsPan = new JPanel(new GridLayout(1, 3));
		valcolsPan.add(equityLab);
		valcolsPan.add(valueLab);
		valcolsPan.add(outsLab);
		
		// rank labels
		JPanel rankcolsPan = new JPanel(new GridLayout(1, 9));
		for (int n = 0; n < 9; n++) {
			JLabel l = new JLabel();
			l.setVerticalAlignment(SwingConstants.CENTER);
			l.setPreferredSize(new Dimension(boldfont.getSize() * 4, boldfont.getSize() + 4));
			l.setMinimumSize(l.getPreferredSize());
			rankLabs[n] = l;
			rankcolsPan.add(rankLabs[n]);
		}
		
		JPanel rowsPan = new JPanel(new GridLayout(2, 1));
		rowsPan.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		rowsPan.add(valcolsPan);
		rowsPan.add(rankcolsPan);
		
		// add to superclass layout
		add(rowsPan, BorderLayout.CENTER);
	}
	
	@Override
	public void clearCards() {
		super.clearCards();
		clearHandEquity();
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
	
	public void setHandEquity(HandEq e) {
		String s = String.format("Win: %.1f%%", e.won);
		if (e.tied != 0) {
			s += String.format("  Split: %.1f%%", e.tied);
		}
		if (!e.exact) {
			s += " (est)";
		}
		equityLab.setText(s);
		if (e.current == 0) {
			valueLab.setText("");
		} else {
			valueLab.setFont(e.curwin ? boldfont : font);
			valueLab.setText(Poker.desc(e.current));
		}
		
		if (e.current == 0 || e.curwin) {
			outsLab.setText("");
			setToolTipText(null);
			
		} else {
			List<String> minl = new ArrayList<String>(), majl = new ArrayList<String>();
			for (Map.Entry<String,Float> me : e.outs.entrySet()) {
				float v = me.getValue();
				if (v > 0) {
					if (v > 50) {
						majl.add(me.getKey());
					} else {
						minl.add(me.getKey());
					}
				}
			}
			Collections.sort(majl, Poker.cmp);
			Collections.sort(minl, Poker.cmp);
			outsLab.setText("Outs: " + majl.size() + " out of " + e.rem);
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