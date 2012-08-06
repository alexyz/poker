package pet.ui.eq;

import java.awt.*;

import javax.swing.*;

import pet.eq.*;

public class RanksPanel extends JPanel {

	// TODO move to pf
	private static final Font font = UIManager.getFont("Label.font");
	private static final Font boldfont = font.deriveFont(Font.BOLD);

	private final JLabel[] rankLabs;

	public RanksPanel() {
		setLayout(new GridLayout(1, Poker.RANKS));

		// high value rank labels
		rankLabs = new JLabel[Poker.RANKS];
		for (int n = 0; n < rankLabs.length; n++) {
			JLabel l = new JLabel();
			l.setVerticalAlignment(SwingConstants.CENTER);
			l.setPreferredSize(new Dimension(boldfont.getSize() * 4, boldfont.getSize() + 4));
			l.setMinimumSize(l.getPreferredSize());
			rankLabs[n] = l;
			add(rankLabs[n]);
		}

	}

	public void clearHandEquity() {
		for (JLabel rl : rankLabs) {
			rl.setFont(font);
			rl.setText("");
		}
	}

	public void setHandEquity(Equity e) {
		if (e.eqtype == Equity.HI_ONLY || e.eqtype == Equity.HILO_HI_HALF) {
			for (int n = 0; n < rankLabs.length; n++) {
				JLabel rl = rankLabs[n];
				rl.setForeground(e.wonrank[n] > 0 ? Color.black : Color.darkGray);
				rl.setFont(e.wonrank[n] > 0 ? boldfont : font);
				rl.setText(String.format("%s: %.0f", Poker.ranknames[n], e.wonrank[n]));
			}
		} else {
			// TODO ranks for other equity types
			rankLabs[0].setText("?");
		}
	}

}
