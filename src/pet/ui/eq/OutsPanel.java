package pet.ui.eq;

import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import pet.eq.Equity;
import pet.eq.PokerUtil;

/** display outs for a hand */
class OutsPanel extends JPanel {
	
	private final Font font = new Font("SansSerif", Font.PLAIN, 12);
	private final JLabel outsLabel = new JLabel();
	
	public OutsPanel() {
		super(new GridLayout(1, 1));
		add(outsLabel);
	}
	
	void clearEquity() {
		outsLabel.setText("");
		setVisible(false);
	}
	
	void setEquity(Equity e, int remCards) {
		if (e.outs == null || e.curwin) {
			// either no outs collected or currently winning
			setVisible(false);
			
		} else {
			List<String> minor = new ArrayList<>(), major = new ArrayList<>();
			for (Equity.Out o : e.outs) {
				if (o.pc > 50) {
					major.add(o.card);
				} else {
					minor.add(o.card);
				}
			}
			
			// make the tool tip
			StringBuilder sb = new StringBuilder("<html>Outs:");
			float f = -1;
			for (Equity.Out o : e.outs) {
				if (f != o.pc) {
					sb.append("<br>").append(String.format("%.1f%%", o.pc)).append(": ");
					f = o.pc;
				}
				sb.append(PokerUtil.cardString(o.card));
			}
			sb.append("</html>");
			outsLabel.setToolTipText(sb.toString());
			
			final int max = 13;
			
			if (major.size() > 0) {
				boolean many = false;
				String[] majorArr = major.toArray(new String[major.size()]);
				if (majorArr.length > max) {
					majorArr = Arrays.copyOf(majorArr, max);
					many = true;
				}
				outsLabel.setFont(font);
				outsLabel.setText("Outs (" + major.size() + "/" + remCards + "): " + PokerUtil.cardsString(majorArr) + (many ? "..." : ""));
				
			} else if (minor.size() > 0) {
				String[] minorArr = minor.toArray(new String[minor.size()]);
				boolean many = false;
				if (minorArr.length > max) {
					minorArr = Arrays.copyOf(minorArr, max);
					many = true;
				}
				outsLabel.setFont(font);
				outsLabel.setText("Minor outs: " + PokerUtil.cardsString(minorArr) + (many ? "..." : ""));
				
			} else {
				outsLabel.setFont(font);
				outsLabel.setText("No outs :(");
			}
			
			setVisible(true);
		}
		
	}
}
