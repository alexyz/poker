package pet.ui.eq;

import java.awt.*;

import javax.swing.*;

import pet.eq.*;

/** show high hand ranks */
public class RanksPanel extends JPanel {
	
	private final Font font = new Font("SansSerif", Font.PLAIN, 12);
	//private final Font boldfont = new Font("SansSerif", Font.BOLD, 12);
	
	private final JLabel[] rankLabs;
	
	public RanksPanel() {
		setLayout(new GridLayout(1, Poker.RANKS));
		
		// high value rank labels
		rankLabs = new JLabel[Poker.RANKS];
		for (int n = 0; n < rankLabs.length; n++) {
			JLabel l = new JLabel();
			l.setVerticalAlignment(SwingConstants.CENTER);
			//l.setPreferredSize(new Dimension(boldfont.getSize() * 4, boldfont.getSize() + 4));
			//l.setMinimumSize(l.getPreferredSize());
			rankLabs[n] = l;
			add(rankLabs[n]);
		}
		
	}
	
	public void clearEquity() {
		for (JLabel rl : rankLabs) {
			rl.setFont(font);
			rl.setText("");
		}
	}
	
	/** populate the rank names and win percentages */
	public void setEquity(Equity e) {
		String[] names = EquityUtil.getRankNames(e.type);
		for (int n = 0; n < rankLabs.length; n++) {
			JLabel rl = rankLabs[n];
			if (names != null && names.length > n) {
				rl.setForeground(e.wonrank[n] > 0 ? Color.black : Color.darkGray);
				//rl.setFont(e.wonrank[n] > 0 ? boldfont : font);
				rl.setFont(font);
				rl.setText(String.format("%s: %.0f", names[n], e.wonrank[n]));
			} else {
				rl.setText("");
			}
		}
	}
	
}
