package pet.ui.eq;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import pet.eq.Equity;
import pet.eq.PokerUtil;

/** display outs for a hand */
class OutsPanel extends JPanel {
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
		if (e.current == 0 || e.curwin) {
			// either no current value or currently winning
			setVisible(false);
			
		} else {
			List<String> minor = new ArrayList<String>(), major = new ArrayList<String>();
			for (Equity.Out o : e.outs) {
				if (o.pc > 50) {
					major.add(o.card);
				} else if (o.pc > 0) {
					minor.add(o.card);
				}
			}
			if (major.size() > 0) {
				String[] majorArr = major.toArray(new String[major.size()]);
				outsLabel.setText("Outs (" + major.size() + "/" + remCards + "): " + PokerUtil.cardsString(majorArr));
			} else if (minor.size() > 0) {
				String[] minorArr = minor.toArray(new String[minor.size()]);
				outsLabel.setText("Minor outs: " + PokerUtil.cardsString(minorArr));
			} else {
				outsLabel.setText("No outs :(");
			}
			setVisible(true);
		}
		
	}
}
